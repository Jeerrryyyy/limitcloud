package com.jevzo.limitcloud.slave.network.protocol.out

import com.jevzo.limitcloud.library.document.Document
import com.jevzo.limitcloud.library.network.protocol.Packet

class PacketOutSlaveReady(
    private val uuid: String
) : Packet {

    constructor() : this("empty")

    override fun write(): Document {
        return Document().appendString("uuid", uuid)
    }
}