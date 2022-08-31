package com.jevzo.limitcloud.slave.process.models

import com.jevzo.limitcloud.library.process.ProcessStage
import com.jevzo.limitcloud.library.process.ProcessStreamConsumer
import com.jevzo.limitcloud.library.process.ProcessType
import java.nio.file.Path

data class CloudProcess(
    val name: String,
    val uuid: String,
    val type: ProcessType,
    val stage: ProcessStage,
    val minMemory: Int,
    val maxMemory: Int,
    val serverDirectoryPath: Path,
    val process: Process,
    val processStreamConsumer: ProcessStreamConsumer
)