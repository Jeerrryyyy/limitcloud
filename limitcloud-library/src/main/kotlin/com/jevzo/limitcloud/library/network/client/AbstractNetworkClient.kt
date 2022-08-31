package com.jevzo.limitcloud.library.network.client

import com.jevzo.limitcloud.library.network.helper.NettyHelper
import io.netty.bootstrap.Bootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.EventLoopGroup
import io.netty.handler.ssl.SslContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory

abstract class AbstractNetworkClient(
    private val nettyHelper: NettyHelper
) {

    private val logger: Logger = LoggerFactory.getLogger(AbstractNetworkClient::class.java)

    private lateinit var workerGroup: EventLoopGroup

    fun startClient(host: String, port: Int) {
        workerGroup = nettyHelper.getEventLoopGroup("limitclient")

        val sslContext = nettyHelper.createClientCert()
        val channelClass = nettyHelper.getClientChannelClass()

        Thread({
            try {
                Bootstrap().group(workerGroup)
                    .option(ChannelOption.AUTO_READ, true)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.IP_TOS, 24)
                    .option(ChannelOption.TCP_NODELAY, true)

                    .channel(channelClass)
                    .handler(object : ChannelInitializer<Channel>() {
                        override fun initChannel(channel: Channel) {
                            preparePipeline(sslContext, channel)
                        }
                    }).connect(host, port).channel().closeFuture().syncUninterruptibly()
            } catch (e: Exception) {
                logger.error(e.message)
            } finally {
                workerGroup.shutdownGracefully()
            }
        }, "limitclient").start()
    }

    fun shutdownGracefully() {
        workerGroup.shutdownGracefully()
    }

    abstract fun preparePipeline(sslContext: SslContext?, channel: Channel)
}