package com.jevzo.limitcloud.master.configuration

import com.jevzo.limitcloud.library.configuration.Configuration
import com.jevzo.limitcloud.library.document.Document
import com.jevzo.limitcloud.library.utils.PortUtils
import com.jevzo.limitcloud.master.configuration.models.DatabaseConfig
import com.jevzo.limitcloud.master.configuration.models.MasterConfig
import com.jevzo.limitcloud.master.configuration.models.MongoDbConfig
import com.jevzo.limitcloud.master.configuration.models.ValidSlaveConfig
import com.jevzo.limitcloud.master.runtime.DirectoryConstants
import com.jevzo.limitcloud.master.runtime.RuntimeVars
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

class DefaultCloudConfiguration(
    private val directoryConstants: DirectoryConstants,
    private val runtimeVars: RuntimeVars
) : Configuration() {

    private val logger: Logger = LoggerFactory.getLogger(DefaultCloudConfiguration::class.java)

    private val availableDatabaseBackends: MutableSet<String> = mutableSetOf(
        "FILE",
        "MONGO"
    )

    override fun execute() {
        val cloudConfigFile = File("${directoryConstants.masterConfigCloud}/config.json")

        if (!cloudConfigFile.exists()) {
            val masterPort = this.readPort("master", 8000)
            val webServerPort = this.readPort("web server", 8080)
            val databaseBackend = this.readDatabaseBackend()

            val masterConfig = MasterConfig(
                masterPort = masterPort,
                webServerPort = webServerPort,
                databaseBackend = databaseBackend,
                validSlaves = this.createValidSlaveConfig(),
                databases = this.createDatabaseConfig()
            )

            MasterConfig.toDocument(masterConfig).write(cloudConfigFile)
            runtimeVars.masterConfig = masterConfig

            logger.warn("It seems the cloud config was just created. Please edit it depending on your wishes!")
        } else {
            runtimeVars.masterConfig = MasterConfig.fromDocument(Document.read(cloudConfigFile))
        }
    }

    private fun readPort(service: String, defaultPort: Int): Int {
        logger.info("Please pick a port for the $service to run on. Default: $defaultPort")
        val input = bufferedReader.readLine()

        if (input.equals("")) {
            return defaultPort
        }

        try {
            val port = Integer.parseInt(input)

            if (!PortUtils.isPortFree(port)) {
                logger.warn("Your entered port is already in use!")
                return this.readPort(service, defaultPort)
            }

            return port
        } catch (e: NumberFormatException) {
            logger.warn("The port needs to be a non floating number!")
            return this.readPort(service, defaultPort)
        }
    }

    private fun readDatabaseBackend(): String {
        logger.info("Please choose a database backend. $availableDatabaseBackends")
        val input = bufferedReader.readLine()

        if (!(availableDatabaseBackends.contains(input.uppercase()))) {
            return this.readDatabaseBackend()
        }

        return input.uppercase()
    }

    private fun createValidSlaveConfig(): MutableList<ValidSlaveConfig> {
        return mutableListOf(
            ValidSlaveConfig(
                slaveName = "Slave-01",
                whitelistedIps = mutableListOf("127.0.0.1")
            )
        )
    }

    private fun createDatabaseConfig(): DatabaseConfig {
        val mongoDbConfig = MongoDbConfig(
            databaseHost = "localhost",
            databaseName = "cloudsystem",
            playerCollectionName = "playerCollection",
            port = 27017,
            username = "<change me>",
            password = "<change me>",
            useAuth = false
        )

        return DatabaseConfig(mongoDbConfig)
    }
}