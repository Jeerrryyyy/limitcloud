package com.jevzo.limitcloud.library.network.web

import com.jevzo.limitcloud.library.network.helper.NettyHelper
import io.netty.bootstrap.ServerBootstrap
import io.netty.buffer.ByteBufAllocator
import io.netty.channel.*
import io.netty.channel.epoll.Epoll
import io.netty.channel.epoll.EpollServerSocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.system.exitProcess

abstract class AbstractWebServer(
    private val nettyHelper: NettyHelper
) {

    private val logger: Logger = LoggerFactory.getLogger(AbstractWebServer::class.java)

    private lateinit var workerGroup: EventLoopGroup

    fun startServer(port: Int) {
        val epoll = Epoll.isAvailable()

        workerGroup = nettyHelper.getEventLoopGroup("webserver")

        val rightChannelClass = if (epoll) EpollServerSocketChannel::class.java else NioServerSocketChannel::class.java

        Thread({
            try {
                ServerBootstrap().group(workerGroup)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.IP_TOS, 24)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childOption(ChannelOption.ALLOCATOR, ByteBufAllocator.DEFAULT)
                    .childOption(ChannelOption.AUTO_READ, true)

                    .option(ChannelOption.ALLOCATOR, ByteBufAllocator.DEFAULT)
                    .option(ChannelOption.AUTO_READ, true)

                    .channel(rightChannelClass)
                    .childHandler(object : ChannelInitializer<Channel>() {
                        override fun initChannel(channel: Channel) {
                            preparePipeline(channel)
                        }
                    }).bind(port)
                    .addListener {
                        if (it.isSuccess) {
                            logger.info("Web Server started and was bound to 127.0.0.1:$port")
                        } else {
                            logger.error("Something went wrong while starting the web server")
                            exitProcess(0)
                        }
                    }
                    .addListener(ChannelFutureListener.CLOSE_ON_FAILURE)
                    .addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE)
                    .channel().closeFuture().syncUninterruptibly()
            } catch (e: Exception) {
                logger.error(e.message)
            } finally {
                workerGroup.shutdownGracefully()
            }
        }, "webserver").start()
    }

    fun shutdownGracefully() {
        workerGroup.shutdownGracefully()
    }

    abstract fun preparePipeline(channel: Channel)
}