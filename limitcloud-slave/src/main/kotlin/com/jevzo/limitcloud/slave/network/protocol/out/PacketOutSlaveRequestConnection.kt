package com.jevzo.limitcloud.slave.network.protocol.out

import com.jevzo.limitcloud.library.document.Document
import com.jevzo.limitcloud.library.dto.SlaveInfo
import com.jevzo.limitcloud.library.network.protocol.Packet

class PacketOutSlaveRequestConnection(
    private val secretKey: String,
    private val slaveInfo: SlaveInfo
) : Packet {

    constructor() : this(
        "empty",
        SlaveInfo(
            "empty", "empty", "empty", "empty", 0, -1L,
            -1L, -1.0, mutableListOf()
        )
    )

    override fun write(): Document {
        return Document().appendString("secretKey", secretKey)
            .appendDocument("slaveInfo", SlaveInfo.toDocument(slaveInfo))
    }
}