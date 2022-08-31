package com.jevzo.limitcloud.slave.configuration.models

import com.jevzo.limitcloud.library.document.Document

class SlaveConfig(
    val masterAddress: String,
    val masterPort: Int,
    val webPort: Int,
    val name: String,
    val delimiter: String,
    val suffix: String,
    val memory: Long,
    val uuid: String,
    val responsibleGroups: MutableList<String>
) {

    companion object {
        fun toDocument(slaveConfig: SlaveConfig): Document {
            return Document().appendString("masterAddress", slaveConfig.masterAddress)
                .appendInt("masterPort", slaveConfig.masterPort)
                .appendInt("webPort", slaveConfig.webPort)
                .appendString("name", slaveConfig.name)
                .appendString("delimiter", slaveConfig.delimiter)
                .appendString("suffix", slaveConfig.suffix)
                .appendLong("memory", slaveConfig.memory)
                .appendString("uuid", slaveConfig.uuid)
                .appendList("responsibleGroups", slaveConfig.responsibleGroups)
        }

        fun fromDocument(document: Document): SlaveConfig {
            return SlaveConfig(
                masterAddress = document.getStringValue("masterAddress"),
                masterPort = document.getIntValue("masterPort"),
                webPort = document.getIntValue("webPort"),
                name = document.getStringValue("name"),
                delimiter = document.getStringValue("delimiter"),
                suffix = document.getStringValue("suffix"),
                memory = document.getLongValue("memory"),
                uuid = document.getStringValue("uuid"),
                responsibleGroups = document.getList("responsibleGroups") as MutableList<String>
            )
        }
    }
}