package com.jevzo.limitcloud.master.configuration.models

data class MongoDbConfig(
    val databaseHost: String,
    val databaseName: String,
    val playerCollectionName: String,
    val port: Int,
    val username: String,
    val password: String,
    val useAuth: Boolean
)