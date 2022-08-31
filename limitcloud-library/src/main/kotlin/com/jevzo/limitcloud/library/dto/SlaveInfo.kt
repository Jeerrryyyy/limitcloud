package com.jevzo.limitcloud.library.dto

import com.jevzo.limitcloud.library.document.Document
import io.netty.channel.Channel

data class SlaveInfo(
    val uuid: String,
    val name: String,
    val delimiter: String,
    val suffix: String,
    var currentOnlineServers: Int,
    val memory: Long,
    var currentMemoryConsumption: Long,
    var currentCpuConsumption: Double,
    val responsibleGroups: MutableList<String>,
    var channel: Channel? = null // Might be null because we don't need it in the packets but in the master
) {
    companion object {
        fun fromDocument(document: Document): SlaveInfo {
            return SlaveInfo(
                uuid = document.getStringValue("uuid"),
                name = document.getStringValue("name"),
                delimiter = document.getStringValue("delimiter"),
                suffix = document.getStringValue("suffix"),
                currentOnlineServers = document.getIntValue("currentOnlineServers"),
                memory = document.getLongValue("memory"),
                currentMemoryConsumption = document.getLongValue("currentMemoryConsumption"),
                currentCpuConsumption = document.getDoubleValue("currentCpuConsumption"),
                responsibleGroups = document.getList("responsibleGroups") as MutableList<String>
            )
        }

        fun toDocument(slaveInfo: SlaveInfo): Document {
            return Document().appendString("uuid", slaveInfo.uuid)
                .appendString("name", slaveInfo.name)
                .appendString("delimiter", slaveInfo.delimiter)
                .appendString("suffix", slaveInfo.suffix)
                .appendInt("currentOnlineServers", slaveInfo.currentOnlineServers)
                .appendLong("memory", slaveInfo.memory)
                .appendLong("currentMemoryConsumption", slaveInfo.currentMemoryConsumption)
                .appendDouble("currentCpuConsumption", slaveInfo.currentCpuConsumption)
                .appendList("responsibleGroups", slaveInfo.responsibleGroups)
        }
    }
}