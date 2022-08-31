package com.jevzo.limitcloud.master.configuration.models

data class ValidSlaveConfig(
    val slaveName: String,
    val whitelistedIps: MutableList<String>
)