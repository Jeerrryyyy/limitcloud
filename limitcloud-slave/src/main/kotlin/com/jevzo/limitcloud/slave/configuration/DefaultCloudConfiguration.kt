package com.jevzo.limitcloud.slave.configuration

import com.jevzo.limitcloud.library.configuration.Configuration
import com.jevzo.limitcloud.library.document.Document
import com.jevzo.limitcloud.library.utils.HardwareUtils
import com.jevzo.limitcloud.slave.configuration.models.SlaveConfig
import com.jevzo.limitcloud.slave.runtime.DirectoryConstants
import com.jevzo.limitcloud.slave.runtime.RuntimeVars
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.util.*

class DefaultCloudConfiguration(
    private val directoryConstants: DirectoryConstants,
    private val runtimeVars: RuntimeVars
) : Configuration() {

    private val logger: Logger = LoggerFactory.getLogger(DefaultCloudConfiguration::class.java)

    override fun execute() {
        val cloudConfigFile = File("${directoryConstants.slaveConfig}/config.json")

        if (!cloudConfigFile.exists()) {
            val masterAddress = this.readMasterAddress()
            val masterPort = this.readMasterPort()
            val webPort = this.readWebPort()
            val slaveName = this.readSlaveName()
            val delimiter = this.readSlaveDelimiter()
            val suffix = this.readSlaveSuffix()
            val memory = this.readSlaveMemory()
            val responsibleGroups = this.readResponsibleGroups()

            val slaveConfig = SlaveConfig(
                masterAddress = masterAddress,
                masterPort = masterPort,
                webPort = webPort,
                name = slaveName,
                delimiter = delimiter,
                suffix = suffix,
                memory = memory,
                uuid = UUID.randomUUID().toString().replace("-", ""),
                responsibleGroups = responsibleGroups
            )

            SlaveConfig.toDocument(slaveConfig).write(cloudConfigFile)
            runtimeVars.slaveConfig = slaveConfig

            logger.warn("It seems the cloud config was just created. Please edit it depending on your wishes!")
        } else {
            runtimeVars.slaveConfig = SlaveConfig.fromDocument(Document.read(cloudConfigFile))
        }
    }

    private fun readMasterAddress(): String {
        logger.info("Please enter the server address of the master. Default: 127.0.0.1")

        val input = bufferedReader.readLine()

        if (input.equals("")) {
            return "127.0.0.1"
        }

        return input
    }

    private fun readMasterPort(): Int {
        logger.info("Please enter the port of the master. Default: 8000")
        val input = bufferedReader.readLine()

        if (input.equals("")) {
            return 8000
        }

        return try {
            Integer.parseInt(input)
        } catch (e: NumberFormatException) {
            logger.warn("The port needs to be a non floating number!")
            this.readMasterPort()
        }
    }

    private fun readWebPort(): Int {
        logger.info("Please enter the port of the web server. Default: 8080")
        val input = bufferedReader.readLine()

        if (input.equals("")) {
            return 8080
        }

        return try {
            Integer.parseInt(input)
        } catch (e: NumberFormatException) {
            logger.warn("The port needs to be a non floating number!")
            this.readMasterPort()
        }
    }

    private fun readSlaveName(): String {
        logger.info("Please enter a name for the slave. Default: Slave")

        val input = bufferedReader.readLine()

        if (input.equals("")) {
            return "Slave"
        }

        return input
    }

    private fun readSlaveDelimiter(): String {
        logger.info("Please enter a delimiter for the slave name. Default: -")

        val input = bufferedReader.readLine()

        if (input.equals("")) {
            return "-"
        }

        return input
    }

    private fun readSlaveSuffix(): String {
        logger.info("Please enter a suffix for the slave name. Default: 01")

        val input = bufferedReader.readLine()

        if (input.equals("")) {
            return "01"
        }

        return input
    }

    private fun readSlaveMemory(): Long {
        logger.info("Please enter the max memory for this slave. Default: ${(HardwareUtils.getSystemMemory() / 3) / 1024 / 1024}")

        val input = bufferedReader.readLine()

        if (input.equals("")) {
            return (HardwareUtils.getSystemMemory() / 3) / 1024 / 1024
        }

        return try {
            input.toLong()
        } catch (e: NumberFormatException) {
            this.readSlaveMemory()
        }
    }

    private fun readResponsibleGroups(): MutableList<String> {
        logger.info("Please enter the responsible groups for the slave. Split them by \",\" Default: All groups")

        val input = bufferedReader.readLine()

        if (input.equals("")) {
            return mutableListOf()
        }

        val groups = input.split("[ ,]+".toRegex())

        logger.info("You chose ${groups.toMutableList()}. Is that right? (Y/n)")

        if (this.yesOrNo()) {
            return groups.toMutableList()
        }

        return this.readResponsibleGroups()
    }

    private fun yesOrNo(): Boolean {
        val input = bufferedReader.readLine()

        if (input.equals("")) {
            return true;
        }

        if (!input.equals("Y", true) && !input.equals("N", true)) {
            logger.warn("Please enter only \"Y\" for yes and \"N\" for no!")
            return this.yesOrNo()
        }

        return input.equals("Y", true)
    }
}