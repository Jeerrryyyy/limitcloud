package com.jevzo.limitcloud.master.slave.protocol.`in`

import com.jevzo.limitcloud.library.document.Document
import com.jevzo.limitcloud.library.network.protocol.Packet
import com.jevzo.limitcloud.master.LimitCloudMaster
import com.jevzo.limitcloud.master.process.handler.handlers.BungeeProcessRequestHandler
import com.jevzo.limitcloud.master.process.handler.handlers.SpigotProcessRequestHandler
import com.jevzo.limitcloud.master.slave.SlaveRegistry
import io.netty.channel.ChannelHandlerContext
import org.kodein.di.instance
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class PacketInSlaveReady : Packet {

    private val logger: Logger = LoggerFactory.getLogger(PacketInSlaveReady::class.java)

    private val slaveRegistry: SlaveRegistry by LimitCloudMaster.KODEIN.instance()
    private val bungeeProcessRequestHandler: BungeeProcessRequestHandler by LimitCloudMaster.KODEIN.instance()
    private val spigotProcessRequestHandler: SpigotProcessRequestHandler by LimitCloudMaster.KODEIN.instance()

    lateinit var uuid: String

    override fun read(document: Document) {
        uuid = document.getStringValue("uuid")
    }

    override fun handle(channelHandlerContext: ChannelHandlerContext) {
        val slaveInfo = slaveRegistry.getSlave(uuid)

        if (slaveInfo == null) {
            logger.error("Got ready message from unknown slave...")
            return
        }

        bungeeProcessRequestHandler.requestProcessesOnConnect(slaveInfo)
        spigotProcessRequestHandler.requestProcessesOnConnect(slaveInfo)
    }
}