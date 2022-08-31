package com.jevzo.limitcloud.slave.configuration

import com.jevzo.limitcloud.library.configuration.Configuration
import com.jevzo.limitcloud.library.utils.FileUtils
import com.jevzo.limitcloud.slave.runtime.DirectoryConstants
import com.jevzo.limitcloud.slave.runtime.RuntimeVars
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import kotlin.system.exitProcess

class SlaveKeyReader(
    private val directoryConstants: DirectoryConstants,
    private val runtimeVars: RuntimeVars
) : Configuration() {

    private val logger: Logger = LoggerFactory.getLogger(SlaveKeyReader::class.java)

    override fun execute() {
        val slaveKeyFile = File("${directoryConstants.slaveSecure}/slave.key")

        if (!slaveKeyFile.exists()) {
            logger.error("Can't find the slave key in \"${directoryConstants.slaveSecure}\"! Did you copy it?")
            exitProcess(0)
        } else {
            runtimeVars.secretKey = FileUtils.readStringFromFile(slaveKeyFile)
        }
    }
}