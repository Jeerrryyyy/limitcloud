package com.jevzo.limitcloud.library.network.protocol.handler

import com.jevzo.limitcloud.library.document.Document
import com.jevzo.limitcloud.library.network.protocol.PacketRegistry
import com.jevzo.limitcloud.library.network.protocol.exceptions.PacketNotFoundException
import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufInputStream
import io.netty.buffer.EmptyByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder

class PacketDecoder(
    private val packetRegistry: PacketRegistry
) : ByteToMessageDecoder() {

    override fun decode(channelHandlerContext: ChannelHandlerContext, byteBuf: ByteBuf, output: MutableList<Any>) {
        if (byteBuf is EmptyByteBuf) return

        val packetId = byteBuf.readInt()
        val packet = packetRegistry.getIncomingPacketById(packetId)
            ?: throw PacketNotFoundException("Packet with ID($packetId) is null!")

        val byteBufInputStream = ByteBufInputStream(byteBuf)
        val document = Document.read(byteBufInputStream.readUTF())

        packet.read(document)
        output.add(packet)
    }
}