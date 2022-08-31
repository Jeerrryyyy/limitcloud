package com.jevzo.limitcloud.master.configuration.models

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.jevzo.limitcloud.library.document.Document

data class MasterConfig(
    val masterPort: Int,
    val webServerPort: Int,
    val databaseBackend: String,
    val validSlaves: MutableList<ValidSlaveConfig>,
    val databases: DatabaseConfig
) {

    companion object {
        fun toDocument(masterConfig: MasterConfig): Document {
            return Document().appendInt("masterPort", masterConfig.masterPort)
                .appendInt("webServerPort", masterConfig.webServerPort)
                .appendString("databaseBackend", masterConfig.databaseBackend)
                .appendJsonElement("validSlaves", createValidSlaveConfig(masterConfig))
                .appendDocument("databases", createDatabaseConfig(masterConfig))
        }

        private fun createValidSlaveConfig(masterConfig: MasterConfig): JsonElement {
            val jsonArray = JsonArray()

            masterConfig.validSlaves.forEach {
                val validSlave = Document().appendString("slaveName", it.slaveName)
                    .appendList("whitelistedIps", it.whitelistedIps)

                jsonArray.add(validSlave.getAsJsonObject())
            }

            return jsonArray
        }

        private fun createDatabaseConfig(masterConfig: MasterConfig): Document {
            val mongoDbConfig = masterConfig.databases.mongoDbConfig

            val mongoConfig = Document().appendString("databaseHost", mongoDbConfig.databaseHost)
                .appendString("databaseName", mongoDbConfig.databaseName)
                .appendString("playerCollectionName", mongoDbConfig.playerCollectionName)
                .appendInt("port", mongoDbConfig.port)
                .appendString("username", mongoDbConfig.username)
                .appendString("password", mongoDbConfig.password)
                .appendBoolean("useAuth", mongoDbConfig.useAuth)

            return Document().appendDocument("mongodb", mongoConfig)
        }

        fun fromDocument(document: Document): MasterConfig {
            return MasterConfig(
                masterPort = document.getIntValue("masterPort"),
                webServerPort = document.getIntValue("webServerPort"),
                databaseBackend = document.getStringValue("databaseBackend"),
                validSlaves = processValidSlaves(document.getJsonElementValue("validSlaves").asJsonArray),
                databases = processDatabases(document.getDocument("databases"))
            )
        }

        private fun processValidSlaves(jsonArray: JsonArray): MutableList<ValidSlaveConfig> {
            return jsonArray.map {
                val validSlaveDocument = Document(it.asJsonObject)
                ValidSlaveConfig(
                    slaveName = validSlaveDocument.getStringValue("slaveName"),
                    whitelistedIps = validSlaveDocument.getList("whitelistedIps") as MutableList<String>
                )
            }.toMutableList()
        }

        private fun processDatabases(document: Document): DatabaseConfig {
            return DatabaseConfig(
                mongoDbConfig = processMongoDbDatabase(Document(document.getJsonElementValue("mongodb").asJsonObject))
            )
        }

        private fun processMongoDbDatabase(document: Document): MongoDbConfig {
            return MongoDbConfig(
                databaseHost = document.getStringValue("databaseHost"),
                databaseName = document.getStringValue("databaseName"),
                playerCollectionName = document.getStringValue("playerCollectionName"),
                port = document.getIntValue("port"),
                username = document.getStringValue("username"),
                password = document.getStringValue("password"),
                useAuth = document.getBooleanValue("useAuth")
            )
        }
    }
}