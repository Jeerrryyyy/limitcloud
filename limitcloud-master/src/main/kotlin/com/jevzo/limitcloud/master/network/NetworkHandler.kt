package com.jevzo.limitcloud.master.network

import com.jevzo.limitcloud.library.network.error.ErrorHandler
import com.jevzo.limitcloud.library.network.protocol.Packet
import com.jevzo.limitcloud.master.LimitCloudMaster
import com.jevzo.limitcloud.master.slave.SlaveRegistry
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import org.kodein.di.instance
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class NetworkHandler : SimpleChannelInboundHandler<Packet>() {

    private val logger: Logger = LoggerFactory.getLogger(NetworkHandler::class.java)

    private val slaveRegistry: SlaveRegistry by LimitCloudMaster.KODEIN.instance()
    private val errorHandler: ErrorHandler by LimitCloudMaster.KODEIN.instance()

    override fun channelRead0(channelHandlerContext: ChannelHandlerContext, packet: Packet) {
        packet.handle(channelHandlerContext)
    }

    override fun channelInactive(channelHandlerContext: ChannelHandlerContext) {
        val slaveInfo = slaveRegistry.getSlaveByChannel(channelHandlerContext.channel()) ?: return
        val slaveName = "${slaveInfo.name}${slaveInfo.delimiter}${slaveInfo.suffix}"

        slaveRegistry.unregisterSlave(slaveInfo.uuid)

        logger.info(
            "Removed connection and unregistered Slave:(Name=$slaveName, Remote=${
                channelHandlerContext.channel().remoteAddress().toString().replace("/", "")
            })"
        )
    }

    @Deprecated("Deprecated in Java",
        ReplaceWith("super.exceptionCaught(ctx, cause)", "io.netty.channel.SimpleChannelInboundHandler")
    )
    override fun exceptionCaught(channelHandlerContext: ChannelHandlerContext, cause: Throwable) {
        errorHandler.handleError(slaveRegistry.getSlaveByChannel(channelHandlerContext.channel()), cause)
    }
}
