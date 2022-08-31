package com.jevzo.limitcloud.library.network.protocol.handler

import com.jevzo.limitcloud.library.network.protocol.Packet
import com.jevzo.limitcloud.library.network.protocol.PacketRegistry
import com.jevzo.limitcloud.library.network.protocol.exceptions.PacketNotFoundException
import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufOutputStream
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToByteEncoder

class PacketEncoder(
    private val packetRegistry: PacketRegistry
) : MessageToByteEncoder<Packet>() {

    override fun encode(channelHandlerContext: ChannelHandlerContext, packet: Packet, byteBuf: ByteBuf) {
        val packetId = packetRegistry.getIdByOutgoingPacket(packet)

        if (packetId == -1) throw PacketNotFoundException("ID of Packet(${packet::class.java.simpleName}) was not found!")
        else {
            byteBuf.writeInt(packetId)

            val byteBufOutputStream = ByteBufOutputStream(byteBuf)
            val document = packet.write()

            byteBufOutputStream.writeUTF(document.getAsString())
        }
    }
}