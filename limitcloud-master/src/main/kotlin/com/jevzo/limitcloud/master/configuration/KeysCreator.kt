package com.jevzo.limitcloud.master.configuration

import com.jevzo.limitcloud.library.configuration.Configuration
import com.jevzo.limitcloud.library.utils.FileUtils
import com.jevzo.limitcloud.master.runtime.DirectoryConstants
import com.jevzo.limitcloud.master.runtime.RuntimeVars
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.util.*

class KeysCreator(
    private val directoryConstants: DirectoryConstants,
    private val runtimeVars: RuntimeVars
) : Configuration() {

    private val logger: Logger = LoggerFactory.getLogger(KeysCreator::class.java)

    override fun execute() {
        val slaveKeyFile = File("${directoryConstants.masterSecure}/slave.key")

        if (!slaveKeyFile.exists()) {
            val slaveKey: String = this.generateKey();

            runtimeVars.secretKey = slaveKey
            FileUtils.writeStringToFile(slaveKeyFile, slaveKey)

            logger.warn("It seems the slave key was just created. Please copy it to the secure directory of the slave!")
        } else {
            runtimeVars.secretKey = FileUtils.readStringFromFile(slaveKeyFile)
        }

        val webKeyFile = File("${directoryConstants.masterSecure}/web.key")

        if (!webKeyFile.exists()) {
            val webKey: String = this.generateKey();

            runtimeVars.webKey = webKey
            FileUtils.writeStringToFile(webKeyFile, webKey)
        } else {
            runtimeVars.webKey = FileUtils.readStringFromFile(webKeyFile)
        }
    }

    private fun generateKey(): String {
        val keyBuilder = StringBuilder()

        (0..10).forEach { _ ->
            keyBuilder.append(UUID.randomUUID().toString().replace("-", ""))
        }

        return keyBuilder.toString()
    }
}