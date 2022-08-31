package com.jevzo.limitcloud.slave.network.protocol.out

import com.jevzo.limitcloud.library.document.Document
import com.jevzo.limitcloud.library.network.protocol.Packet

class PacketOutSlaveUpdateLoadStatus(
    private val uuid: String,
    private val currentOnlineServers: Int,
    private val currentMemoryConsumption: Long,
    private val currentCpuConsumption: Double
) : Packet {

    constructor() : this("empty", 0, -1L, -1.0)

    override fun write(): Document {
        return Document().appendString("uuid", uuid)
            .appendInt("currentOnlineServers", currentOnlineServers)
            .appendLong("currentMemoryConsumption", currentMemoryConsumption)
            .appendDouble("currentCpuConsumption", currentCpuConsumption)
    }
}