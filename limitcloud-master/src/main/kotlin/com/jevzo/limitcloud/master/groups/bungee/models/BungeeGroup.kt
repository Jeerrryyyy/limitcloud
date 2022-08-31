package com.jevzo.limitcloud.master.groups.bungee.models

import com.jevzo.limitcloud.library.document.Document
import com.jevzo.limitcloud.master.groups.AbstractGroup

open class BungeeGroup(
    name: String,
    maxServersOnline: Int,
    minServersOnline: Int,
    maxMemory: Int,
    minMemory: Int,
    maxPlayers: Int,
    joinPower: Int,
    maintenance: Boolean,
) : AbstractGroup(name, maxServersOnline, minServersOnline, maxMemory, minMemory, maxPlayers, joinPower, maintenance) {

    companion object {
        fun toDocument(bungeeGroup: BungeeGroup): Document {
            return Document().appendString("name", bungeeGroup.name)
                .appendInt("maxServersOnline", bungeeGroup.maxServersOnline)
                .appendInt("minServersOnline", bungeeGroup.minServersOnline)
                .appendInt("maxMemory", bungeeGroup.maxMemory)
                .appendInt("minMemory", bungeeGroup.minMemory)
                .appendInt("maxPlayers", bungeeGroup.maxPlayers)
                .appendInt("joinPower", bungeeGroup.joinPower)
                .appendBoolean("maintenance", bungeeGroup.maintenance)
        }

        fun fromDocument(document: Document): BungeeGroup {
            return BungeeGroup(
                name = document.getStringValue("name"),
                maxServersOnline = document.getIntValue("maxServersOnline"),
                minServersOnline = document.getIntValue("minServersOnline"),
                maxMemory = document.getIntValue("maxMemory"),
                minMemory = document.getIntValue("minMemory"),
                maxPlayers = document.getIntValue("maxPlayers"),
                joinPower = document.getIntValue("joinPower"),
                maintenance = document.getBooleanValue("maintenance")
            )
        }
    }
}