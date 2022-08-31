package com.jevzo.limitcloud.master.process.models

import com.jevzo.limitcloud.library.process.ProcessStage
import com.jevzo.limitcloud.library.process.ProcessType

abstract class AbstractProcess(
    val groupName: String,
    val name: String,
    val uuid: String,
    val ip: String,
    val type: ProcessType,
    val stage: ProcessStage,
    val minMemory: Int,
    val maxMemory: Int,
    val port: Int,
    val maxPlayers: Int,
    val joinPower: Int,
    val maintenance: Boolean,
)