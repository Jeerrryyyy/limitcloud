package com.jevzo.limitcloud.master.slave.protocol.`in`

import com.jevzo.limitcloud.library.document.Document
import com.jevzo.limitcloud.library.network.protocol.Packet
import com.jevzo.limitcloud.master.LimitCloudMaster
import com.jevzo.limitcloud.master.slave.SlaveRegistry
import io.netty.channel.ChannelHandlerContext
import org.kodein.di.instance
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.properties.Delegates

class PacketInSlaveUpdateLoadStatus : Packet {

    private val logger: Logger = LoggerFactory.getLogger(PacketInSlaveUpdateLoadStatus::class.java)

    private val slaveRegistry: SlaveRegistry by LimitCloudMaster.KODEIN.instance()

    lateinit var uuid: String

    var currentOnlineServers by Delegates.notNull<Int>()
    var currentMemoryConsumption by Delegates.notNull<Long>()
    var currentCpuConsumption by Delegates.notNull<Double>()

    override fun read(document: Document) {
        uuid = document.getStringValue("uuid")
        currentOnlineServers = document.getIntValue("currentOnlineServers")
        currentMemoryConsumption = document.getLongValue("currentMemoryConsumption")
        currentCpuConsumption = document.getDoubleValue("currentCpuConsumption")
    }

    override fun handle(channelHandlerContext: ChannelHandlerContext) {
        val slaveInfo = slaveRegistry.getSlave(uuid)

        if (slaveInfo == null) {
            logger.error("Internal error occurred while updating load status of Slave:(Uuid=$uuid)")
            return
        }

        slaveInfo.currentOnlineServers = currentOnlineServers
        slaveInfo.currentMemoryConsumption = currentMemoryConsumption
        slaveInfo.currentCpuConsumption = currentCpuConsumption

        slaveRegistry.updateSlave(uuid, slaveInfo)
    }
}