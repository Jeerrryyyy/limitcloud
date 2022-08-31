package com.jevzo.limitcloud.slave.network.protocol.`in`

import com.jevzo.limitcloud.library.document.Document
import com.jevzo.limitcloud.library.network.protocol.Packet
import com.jevzo.limitcloud.slave.LimitCloudSlave
import com.jevzo.limitcloud.slave.runtime.RuntimeVars
import io.netty.channel.ChannelHandlerContext
import org.kodein.di.instance
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.properties.Delegates

class PacketInSlaveConnectionEstablished : Packet {

    private val logger: Logger = LoggerFactory.getLogger(PacketInSlaveConnectionEstablished::class.java)

    private val runtimeVars: RuntimeVars by LimitCloudSlave.KODEIN.instance()

    private lateinit var message: String
    private lateinit var webKey: String

    private var successful by Delegates.notNull<Boolean>()

    override fun read(document: Document) {
        message = document.getStringValue("message")
        webKey = document.getStringValue("webKey")
        successful = document.getBooleanValue("successful")
    }

    override fun handle(channelHandlerContext: ChannelHandlerContext) {
        if (successful) {
            runtimeVars.webKey = webKey
            logger.info(message)
        } else logger.error(message)
    }
}