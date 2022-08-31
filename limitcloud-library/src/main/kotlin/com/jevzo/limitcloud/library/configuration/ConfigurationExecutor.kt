package com.jevzo.limitcloud.library.configuration

class ConfigurationExecutor {

    private val configurations: MutableList<Configuration> = mutableListOf()

    fun registerConfiguration(configuration: Configuration) {
        configurations.add(configuration)
    }

    fun executeConfigurations() {
        configurations.forEach { it.execute() }
    }
}