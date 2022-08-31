package com.jevzo.limitcloud.slave.process.protocol.`in`

import com.jevzo.limitcloud.library.document.Document
import com.jevzo.limitcloud.library.network.protocol.Packet
import com.jevzo.limitcloud.library.process.ProcessStage
import com.jevzo.limitcloud.library.process.ProcessType
import com.jevzo.limitcloud.slave.LimitCloudSlave
import com.jevzo.limitcloud.slave.process.handler.SpigotProcessRequestHandler
import io.netty.channel.ChannelHandlerContext
import org.kodein.di.instance
import kotlin.properties.Delegates

class PacketInRequestSpigotProcess : Packet {

    private val spigotProcessRequestHandler: SpigotProcessRequestHandler by LimitCloudSlave.KODEIN.instance()

    lateinit var groupName: String
    lateinit var name: String
    lateinit var uuid: String
    lateinit var ip: String
    lateinit var type: ProcessType
    lateinit var stage: ProcessStage

    var minMemory by Delegates.notNull<Int>()
    var maxMemory by Delegates.notNull<Int>()
    var port by Delegates.notNull<Int>()
    var maxPlayers by Delegates.notNull<Int>()
    var joinPower by Delegates.notNull<Int>()
    var maintenance by Delegates.notNull<Boolean>()
    var lobbyServer by Delegates.notNull<Boolean>()
    var dynamicServer by Delegates.notNull<Boolean>()
    var staticServer by Delegates.notNull<Boolean>()

    override fun read(document: Document) {
        groupName = document.getStringValue("groupName")
        name = document.getStringValue("name")
        uuid = document.getStringValue("uuid")
        ip = document.getStringValue("ip")
        type = ProcessType.valueOf(document.getStringValue("type"))
        stage = ProcessStage.valueOf(document.getStringValue("stage"))
        minMemory = document.getIntValue("minMemory")
        maxMemory = document.getIntValue("maxMemory")
        port = document.getIntValue("port")
        maxPlayers = document.getIntValue("maxPlayers")
        joinPower = document.getIntValue("joinPower")
        maintenance = document.getBooleanValue("maintenance")
        lobbyServer = document.getBooleanValue("lobbyServer")
        dynamicServer = document.getBooleanValue("dynamicServer")
        staticServer = document.getBooleanValue("staticServer")
    }

    override fun handle(channelHandlerContext: ChannelHandlerContext) {
        spigotProcessRequestHandler.handle(groupName, name, uuid, type, stage, port, maxPlayers, minMemory, maxMemory)
    }
}