package com.jevzo.limitcloud.master.process.protocol.out

import com.jevzo.limitcloud.library.document.Document
import com.jevzo.limitcloud.library.network.protocol.Packet
import com.jevzo.limitcloud.master.process.models.SpigotProcess

class PacketOutRequestSpigotProcess(
    private val spigotProcess: SpigotProcess?
) : Packet {

    constructor() : this(null)

    override fun write(): Document {
        return Document().appendString("groupName", spigotProcess!!.groupName)
            .appendString("name", spigotProcess.name)
            .appendString("uuid", spigotProcess.uuid)
            .appendString("ip", spigotProcess.ip)
            .appendString("type", spigotProcess.type.toString())
            .appendString("stage", spigotProcess.stage.toString())
            .appendInt("minMemory", spigotProcess.minMemory)
            .appendInt("maxMemory", spigotProcess.maxMemory)
            .appendInt("port", spigotProcess.port)
            .appendInt("maxPlayers", spigotProcess.maxPlayers)
            .appendInt("joinPower", spigotProcess.joinPower)
            .appendBoolean("maintenance", spigotProcess.maintenance)
            .appendBoolean("lobbyServer", spigotProcess.lobbyServer)
            .appendBoolean("dynamicServer", spigotProcess.dynamicServer)
            .appendBoolean("staticServer", spigotProcess.staticServer)
    }
}