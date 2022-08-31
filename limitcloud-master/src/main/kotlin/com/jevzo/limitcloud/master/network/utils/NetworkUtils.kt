package com.jevzo.limitcloud.master.network.utils

import com.jevzo.limitcloud.library.network.protocol.Packet
import io.netty.channel.Channel
import io.netty.channel.ChannelFuture

class NetworkUtils {

    fun sendPacket(packet: Packet, channel: Channel): ChannelFuture? {
        return channel.writeAndFlush(packet)
    }
}