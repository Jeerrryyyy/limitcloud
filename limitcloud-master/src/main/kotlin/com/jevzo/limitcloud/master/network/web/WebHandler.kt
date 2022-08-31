package com.jevzo.limitcloud.master.network.web

import com.jevzo.limitcloud.master.LimitCloudMaster
import com.jevzo.limitcloud.master.network.web.router.Router
import com.jevzo.limitcloud.master.runtime.DirectoryConstants
import com.jevzo.limitcloud.master.runtime.RuntimeVars
import io.netty.buffer.Unpooled
import io.netty.channel.*
import io.netty.handler.codec.http.*
import io.netty.handler.ssl.SslHandler
import io.netty.handler.stream.ChunkedFile
import io.netty.util.CharsetUtil
import io.netty.util.internal.SystemPropertyUtil
import org.kodein.di.instance
import java.io.File
import java.io.FileNotFoundException
import java.io.RandomAccessFile
import java.net.URLDecoder
import java.util.regex.Pattern
import javax.activation.MimetypesFileTypeMap

class WebHandler : SimpleChannelInboundHandler<FullHttpRequest>() {

    private val router: Router by LimitCloudMaster.KODEIN.instance()
    private val runtimeVars: RuntimeVars by LimitCloudMaster.KODEIN.instance()
    private val directoryConstants: DirectoryConstants by LimitCloudMaster.KODEIN.instance()

    private val insecureUriPattern = Pattern.compile(".*[<>&\"].*")
    private val allowedFileNamePattern = Pattern.compile("[A-Za-z0-9][-_A-Za-z0-9\\.]*")

    override fun channelRead0(channelHandlerContext: ChannelHandlerContext, fullHttpRequest: FullHttpRequest) {
        if (!fullHttpRequest.decoderResult().isSuccess) {
            this.sendError(channelHandlerContext, HttpResponseStatus.BAD_REQUEST)
            return
        }

        val uri = fullHttpRequest.uri()

        if (!uri.startsWith("/${directoryConstants.masterWeb}/")) {
            val route = router.getRoute(uri)

            if (route == null) {
                this.sendError(channelHandlerContext, HttpResponseStatus.NOT_FOUND)
                return
            }

            val httpResponse = route.handle(channelHandlerContext, fullHttpRequest)

            if (httpResponse == null) {
                this.sendError(channelHandlerContext, HttpResponseStatus.INTERNAL_SERVER_ERROR)
                return
            }

            channelHandlerContext.writeAndFlush(httpResponse).addListener(ChannelFutureListener.CLOSE)
            return
        }

        if (fullHttpRequest.method() != HttpMethod.GET) {
            this.sendError(channelHandlerContext, HttpResponseStatus.METHOD_NOT_ALLOWED)
            return
        }

        if (!fullHttpRequest.headers().contains("X-cloud-auth-key")) {
            this.sendError(channelHandlerContext, HttpResponseStatus.UNAUTHORIZED)
            return
        }

        val cloudAuthKey = fullHttpRequest.headers().get("X-cloud-auth-key")
        if (cloudAuthKey != runtimeVars.webKey) {
            this.sendError(channelHandlerContext, HttpResponseStatus.UNAUTHORIZED)
            return
        }

        val path = this.sanitizeUri(uri)

        if (path == null) {
            this.sendError(channelHandlerContext, HttpResponseStatus.FORBIDDEN)
            return
        }

        val file = File(path)
        if (file.isDirectory) {
            if (uri.endsWith("/")) {
                this.sendListing(channelHandlerContext, file)
            } else {
                this.sendRedirect(channelHandlerContext, "$uri/")
            }
            return
        }

        if (!file.isFile) {
            this.sendError(channelHandlerContext, HttpResponseStatus.FORBIDDEN)
            return
        }

        val randomAccessFile: RandomAccessFile

        try {
            randomAccessFile = RandomAccessFile(file, "r")
        } catch (e: FileNotFoundException) {
            this.sendError(channelHandlerContext, HttpResponseStatus.NOT_FOUND)
            return
        }

        val fileLength = randomAccessFile.length()
        val httpResponse = DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK)

        HttpUtil.setContentLength(httpResponse, fileLength)

        this.setContentTypeHeader(httpResponse, file)

        if (HttpUtil.isKeepAlive(fullHttpRequest)) {
            httpResponse.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE)
        }

        channelHandlerContext.write(httpResponse)

        val lastContentFuture: ChannelFuture =
            if (channelHandlerContext.pipeline().get(SslHandler::class.java) == null) {
                channelHandlerContext.write(
                    DefaultFileRegion(randomAccessFile.channel, 0, fileLength),
                    channelHandlerContext.newProgressivePromise()
                )
                channelHandlerContext.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT)
            } else {
                channelHandlerContext.writeAndFlush(
                    HttpChunkedInput(ChunkedFile(randomAccessFile, 0, fileLength, 8192)),
                    channelHandlerContext.newProgressivePromise()
                )
            }

        if (!HttpUtil.isKeepAlive(fullHttpRequest)) {
            lastContentFuture.addListener(ChannelFutureListener.CLOSE)
        }
    }

    @Deprecated("Deprecated in Java",
        ReplaceWith("super.exceptionCaught(ctx, cause)", "io.netty.channel.SimpleChannelInboundHandler")
    )
    override fun exceptionCaught(channelHandlerContext: ChannelHandlerContext, cause: Throwable) {
        if (channelHandlerContext.channel().isActive) {
            this.sendError(channelHandlerContext, HttpResponseStatus.INTERNAL_SERVER_ERROR)
            return
        }
    }

    private fun sanitizeUri(uri: String): String? {
        var newUri = URLDecoder.decode(uri, CharsetUtil.UTF_8)

        if (newUri.isEmpty() || newUri[0] != '/') {
            return null
        }

        newUri = newUri.replace('/', File.separatorChar)

        if (newUri.contains(File.separator + '.') ||
            newUri.contains('.' + File.separator) ||
            newUri[0] == '.' || newUri[newUri.length - 1] == '.' ||
            insecureUriPattern.matcher(newUri).matches()
        ) {
            return null
        }

        return SystemPropertyUtil.get("user.dir") + newUri
    }

    private fun sendListing(channelHandlerContext: ChannelHandlerContext, directory: File) {
        val httpResponse = DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK)
        httpResponse.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=UTF-8")

        val buffer = StringBuilder().append("<!DOCTYPE html>\r\n")
            .append("<html><head><title>")
            .append("Listing")
            .append("</title></head><body>\r\n")
            .append("<h3>Listing")
            .append("</h3>\r\n")
            .append("<ul>")
            .append("<li><a href=\"../\">..</a></li>\r\n")

        val fileList = directory.listFiles()
        if (fileList != null) {
            for (file in fileList) {
                if (file.isHidden || !file.canRead()) continue

                val name = file.name
                if (!allowedFileNamePattern.matcher(name).matches()) continue

                buffer.append("<li><a href=\"")
                    .append(name)

                if (file.isDirectory) buffer.append("/\">")
                else buffer.append("\">")

                buffer.append(name)
                    .append("</a></li>\r\n")
            }
        }

        buffer.append("</ul></body></html>\r\n")
        val byteBuf = Unpooled.copiedBuffer(buffer, CharsetUtil.UTF_8)
        httpResponse.content().writeBytes(byteBuf)
        byteBuf.release()

        channelHandlerContext.writeAndFlush(httpResponse).addListener(ChannelFutureListener.CLOSE)
    }

    private fun sendRedirect(channelHandlerContext: ChannelHandlerContext, newUri: String) {
        val httpResponse = DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK)
        httpResponse.headers().set(HttpHeaderNames.LOCATION, newUri)

        channelHandlerContext.writeAndFlush(httpResponse).addListener(ChannelFutureListener.CLOSE)
    }

    private fun sendError(channelHandlerContext: ChannelHandlerContext, httpResponseStatus: HttpResponseStatus) {
        val httpResponse = DefaultFullHttpResponse(
            HttpVersion.HTTP_1_1,
            httpResponseStatus,
            Unpooled.copiedBuffer("Failure: $httpResponseStatus\r\n", CharsetUtil.UTF_8)
        )
        httpResponse.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8")

        channelHandlerContext.writeAndFlush(httpResponse).addListener(ChannelFutureListener.CLOSE)
    }

    private fun setContentTypeHeader(httpResponse: HttpResponse, file: File) {
        val mimetypesFileTypeMap = MimetypesFileTypeMap()
        httpResponse.headers().set(HttpHeaderNames.CONTENT_TYPE, mimetypesFileTypeMap.getContentType(file))
    }
}