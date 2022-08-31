package com.jevzo.limitcloud.library.network.protocol

import com.jevzo.limitcloud.library.document.Document
import io.netty.channel.ChannelHandlerContext

interface Packet {

    fun read(document: Document) {}
    fun write(): Document = Document().appendString("message", "no_data")
    fun handle(channelHandlerContext: ChannelHandlerContext) {}
}