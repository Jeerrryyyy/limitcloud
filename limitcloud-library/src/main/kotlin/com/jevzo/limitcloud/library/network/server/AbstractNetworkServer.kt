package com.jevzo.limitcloud.library.network.server

import com.jevzo.limitcloud.library.network.helper.NettyHelper
import io.netty.bootstrap.ServerBootstrap
import io.netty.buffer.ByteBufAllocator
import io.netty.channel.*
import io.netty.handler.ssl.SslContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.system.exitProcess

abstract class AbstractNetworkServer(
    private val nettyHelper: NettyHelper
) {

    private val logger: Logger = LoggerFactory.getLogger(AbstractNetworkServer::class.java)

    private lateinit var bossGroup: EventLoopGroup
    private lateinit var workerGroup: EventLoopGroup

    fun startServer(port: Int) {
        bossGroup = nettyHelper.getEventLoopGroup("limitserver")
        workerGroup = nettyHelper.getEventLoopGroup("limitserver")

        val sslContext = nettyHelper.createServerCert()
        val channelClass = nettyHelper.getServerChannelClass()

        Thread({
            try {
                ServerBootstrap().group(bossGroup, workerGroup)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.IP_TOS, 24)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childOption(ChannelOption.ALLOCATOR, ByteBufAllocator.DEFAULT)
                    .childOption(ChannelOption.AUTO_READ, true)

                    .option(ChannelOption.ALLOCATOR, ByteBufAllocator.DEFAULT)
                    .option(ChannelOption.AUTO_READ, true)

                    .channel(channelClass)
                    .childHandler(object : ChannelInitializer<Channel>() {
                        override fun initChannel(channel: Channel) {
                            preparePipeline(sslContext, channel)
                        }
                    }).bind(port)
                    .addListener {
                        if (it.isSuccess) logger.info("Network Server started and was bound to 127.0.0.1:$port")
                        else {
                            logger.error("Something went wrong while starting the server")
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
                bossGroup.shutdownGracefully()
            }
        }, "limitserver").start()
    }

    fun shutdownGracefully() {
        workerGroup.shutdownGracefully()
        bossGroup.shutdownGracefully()
    }

    abstract fun preparePipeline(sslContext: SslContext?, channel: Channel)
}