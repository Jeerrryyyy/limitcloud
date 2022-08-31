package com.jevzo.limitcloud.library.network.protocol

enum class PacketId(val packetId: Int) {

    PACKET_REQUEST_CONNECTION(1),
    PACKET_ESTABLISHED_CONNECTION(2),
    PACKET_UPDATE_LOAD_STATUS(3),
    PACKET_REQUEST_BUNGEE_PROCESS(4),
    PACKET_REQUEST_SPIGOT_PROCESS(5)
}