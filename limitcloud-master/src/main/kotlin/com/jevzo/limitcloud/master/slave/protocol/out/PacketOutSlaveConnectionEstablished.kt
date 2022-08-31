package com.jevzo.limitcloud.master.slave.protocol.out

import com.jevzo.limitcloud.library.document.Document
import com.jevzo.limitcloud.library.network.protocol.Packet

class PacketOutSlaveConnectionEstablished(
    private val message: String,
    private val webKey: String,
    private val successful: Boolean
) : Packet {

    constructor() : this("empty", "empty", false)

    override fun write(): Document {
        return Document().appendString("message", message)
            .appendString("webKey", webKey)
            .appendBoolean("successful", successful)
    }
}