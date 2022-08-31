package com.jevzo.limitcloud.master.process.protocol.out

import com.jevzo.limitcloud.library.document.Document
import com.jevzo.limitcloud.library.network.protocol.Packet
import com.jevzo.limitcloud.master.process.models.BungeeProcess

class PacketOutRequestBungeeProcess(
    private val bungeeProcess: BungeeProcess?
) : Packet {

    constructor() : this(null)

    override fun write(): Document {
        return Document().appendString("groupName", bungeeProcess!!.groupName)
            .appendString("name", bungeeProcess.name)
            .appendString("uuid", bungeeProcess.uuid)
            .appendString("ip", bungeeProcess.ip)
            .appendString("type", bungeeProcess.type.toString())
            .appendString("stage", bungeeProcess.stage.toString())
            .appendInt("minMemory", bungeeProcess.minMemory)
            .appendInt("maxMemory", bungeeProcess.maxMemory)
            .appendInt("port", bungeeProcess.port)
            .appendInt("maxPlayers", bungeeProcess.maxPlayers)
            .appendInt("joinPower", bungeeProcess.joinPower)
            .appendBoolean("maintenance", bungeeProcess.maintenance)
    }
}