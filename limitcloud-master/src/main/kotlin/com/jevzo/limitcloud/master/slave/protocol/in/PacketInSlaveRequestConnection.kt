package com.jevzo.limitcloud.master.slave.protocol.`in`

import com.jevzo.limitcloud.library.document.Document
import com.jevzo.limitcloud.library.dto.SlaveInfo
import com.jevzo.limitcloud.library.network.protocol.Packet
import com.jevzo.limitcloud.master.LimitCloudMaster
import com.jevzo.limitcloud.master.network.utils.NetworkUtils
import com.jevzo.limitcloud.master.process.handler.handlers.BungeeProcessRequestHandler
import com.jevzo.limitcloud.master.process.handler.handlers.SpigotProcessRequestHandler
import com.jevzo.limitcloud.master.runtime.RuntimeVars
import com.jevzo.limitcloud.master.slave.SlaveRegistry
import com.jevzo.limitcloud.master.slave.protocol.out.PacketOutSlaveConnectionEstablished
import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import org.kodein.di.instance
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.properties.Delegates

class PacketInSlaveRequestConnection : Packet {

    private val logger: Logger = LoggerFactory.getLogger(PacketInSlaveRequestConnection::class.java)

    private val networkUtils: NetworkUtils by LimitCloudMaster.KODEIN.instance()
    private val slaveRegistry: SlaveRegistry by LimitCloudMaster.KODEIN.instance()
    private val runtimeVars: RuntimeVars by LimitCloudMaster.KODEIN.instance()

    private val bungeeProcessRequestHandler: BungeeProcessRequestHandler by LimitCloudMaster.KODEIN.instance()
    private val spigotProcessRequestHandler: SpigotProcessRequestHandler by LimitCloudMaster.KODEIN.instance()

    private lateinit var slaveInfo: SlaveInfo

    private var verified by Delegates.notNull<Boolean>()

    override fun read(document: Document) {
        val secretKey = document.getStringValue("secretKey")

        verified = secretKey == runtimeVars.secretKey
        slaveInfo = SlaveInfo.fromDocument(document.getDocument("slaveInfo"))
    }

    override fun handle(channelHandlerContext: ChannelHandlerContext) {
        slaveInfo.channel = channelHandlerContext.channel()
        val slaveName = "${slaveInfo.name}${slaveInfo.delimiter}${slaveInfo.suffix}"

        if (!this.isSlaveAuthenticated(channelHandlerContext.channel(), slaveName)) {
            networkUtils.sendPacket(
                PacketOutSlaveConnectionEstablished(
                    message = "You are not authenticated. Please check your key or the master config!",
                    webKey = "null",
                    successful = false
                ), channelHandlerContext.channel()
            )
            logger.warn(
                "Blocked connection of unauthenticated Slave:(Name=$slaveName, Remote=${
                    channelHandlerContext.channel().remoteAddress().toString().replace("/", "")
                })"
            )
            return
        }

        val registered = slaveRegistry.registerSlave(slaveInfo)

        if (!registered) {
            networkUtils.sendPacket(
                PacketOutSlaveConnectionEstablished(
                    message = "A slave with this uuid already exists!",
                    webKey = "null",
                    successful = false
                ), channelHandlerContext.channel()
            )
            logger.warn(
                "Blocked connection of unauthenticated Slave:(Name=$slaveName, Remote=${
                    channelHandlerContext.channel().remoteAddress().toString().replace("/", "")
                })"
            )
            return
        }

        networkUtils.sendPacket(
            PacketOutSlaveConnectionEstablished(
                message = "Connection to master established!",
                webKey = runtimeVars.webKey,
                successful = true
            ), channelHandlerContext.channel()
        )
        logger.info(
            "New incoming connection of Slave:(Name=$slaveName, Remote=${
                channelHandlerContext.channel().remoteAddress().toString().replace("/", "")
            })"
        )

        spigotProcessRequestHandler.requestProcessesOnConnect(slaveInfo)
        bungeeProcessRequestHandler.requestProcessesOnConnect(slaveInfo)
    }

    private fun isSlaveAuthenticated(channel: Channel, slaveName: String): Boolean {
        if (!verified) return false

        var returnValue = false
        val validSlaves = runtimeVars.masterConfig.validSlaves

        validSlaves.forEach {
            val validSlaveName = it.slaveName
            val validSlaveWhitelistedIps = it.whitelistedIps

            if (validSlaveName == slaveName) {
                val slaveIpAddress = channel.remoteAddress().toString().replace("/", "").split(":")[0]

                if (validSlaveWhitelistedIps.contains(slaveIpAddress)) {
                    returnValue = true
                }
            }
        }

        return returnValue
    }
}