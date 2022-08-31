package com.jevzo.limitcloud.master.groups

abstract class AbstractGroup(
    val name: String,
    var maxServersOnline: Int,
    var minServersOnline: Int,
    var maxMemory: Int,
    var minMemory: Int,
    var maxPlayers: Int,
    var joinPower: Int,
    var maintenance: Boolean,
)