package com.jevzo.limitcloud.slave.network

import com.jevzo.limitcloud.library.dto.SlaveInfo
import com.jevzo.limitcloud.library.network.error.ErrorHandler
import com.jevzo.limitcloud.library.network.protocol.Packet
import com.jevzo.limitcloud.library.utils.HardwareUtils
import com.jevzo.limitcloud.slave.LimitCloudSlave
import com.jevzo.limitcloud.slave.network.protocol.out.PacketOutSlaveRequestConnection
import com.jevzo.limitcloud.slave.network.utils.NetworkUtils
import com.jevzo.limitcloud.slave.runtime.RuntimeVars
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import org.kodein.di.instance
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.system.exitProcess

class NetworkHandler : SimpleChannelInboundHandler<Packet>() {

    private val logger: Logger = LoggerFactory.getLogger(NetworkHandler::class.java)

    private val runtimeVars: RuntimeVars by LimitCloudSlave.KODEIN.instance()
    private val networkUtils: NetworkUtils by LimitCloudSlave.KODEIN.instance()
    private val errorHandler: ErrorHandler by LimitCloudSlave.KODEIN.instance()

    override fun channelRead0(channelHandlerContext: ChannelHandlerContext, packet: Packet) {
        packet.handle(channelHandlerContext)
    }

    override fun channelActive(channelHandlerContext: ChannelHandlerContext) {
        logger.info("Trying handshake with master server...")

        networkUtils.sendPacket(
            PacketOutSlaveRequestConnection(
                secretKey = runtimeVars.secretKey,
                slaveInfo = SlaveInfo(
                    uuid = runtimeVars.slaveConfig.uuid,
                    name = runtimeVars.slaveConfig.name,
                    delimiter = runtimeVars.slaveConfig.delimiter,
                    suffix = runtimeVars.slaveConfig.suffix,
                    currentOnlineServers = 0,
                    memory = runtimeVars.slaveConfig.memory,
                    currentMemoryConsumption = HardwareUtils.getRuntimeMemoryUsage(),
                    currentCpuConsumption = HardwareUtils.getCpuUsage(),
                    responsibleGroups = runtimeVars.slaveConfig.responsibleGroups
                )
            ), channelHandlerContext.channel()
        )

        runtimeVars.masterChannel = channelHandlerContext.channel()
    }

    override fun channelInactive(channelHandlerContext: ChannelHandlerContext) {
        logger.info("Master unfortunately disconnected, trying to shut down gracefully...")
        exitProcess(-1)
    }

    @Deprecated(
        "Deprecated in Java",
        ReplaceWith("super.exceptionCaught(ctx, cause)", "io.netty.channel.SimpleChannelInboundHandler")
    )
    override fun exceptionCaught(channelHandlerContext: ChannelHandlerContext, cause: Throwable) {
        errorHandler.handleError(null, cause)
    }
}