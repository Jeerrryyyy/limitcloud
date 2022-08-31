package com.jevzo.limitcloud.slave.process.protocol.`in`

import com.jevzo.limitcloud.library.document.Document
import com.jevzo.limitcloud.library.network.protocol.Packet
import io.netty.channel.ChannelHandlerContext

class PacketInRequestBungeeProcess : Packet {

    override fun read(document: Document) {
        println(document.getAsString())
    }

    override fun handle(channelHandlerContext: ChannelHandlerContext) {

    }
}