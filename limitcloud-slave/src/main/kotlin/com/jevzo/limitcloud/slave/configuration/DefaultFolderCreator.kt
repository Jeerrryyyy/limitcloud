package com.jevzo.limitcloud.slave.configuration

import com.jevzo.limitcloud.library.configuration.Configuration
import com.jevzo.limitcloud.library.utils.FileUtils
import com.jevzo.limitcloud.slave.runtime.DirectoryConstants
import java.nio.file.Path

class DefaultFolderCreator(
    private val directoryConstants: DirectoryConstants
) : Configuration() {

    private val requiredFolders: MutableList<Path> = mutableListOf(
        Path.of(directoryConstants.slave),
        Path.of(directoryConstants.slaveCached),
        Path.of(directoryConstants.slaveCachedStatic),
        Path.of(directoryConstants.slaveCachedTemplates),
        Path.of(directoryConstants.slaveCachedTemplatesBungee),
        Path.of(directoryConstants.slaveCachedTemplatesSpigot),
        Path.of(directoryConstants.slaveSecure),
        Path.of(directoryConstants.slaveConfig),
        Path.of(directoryConstants.slaveRunning)
    )

    override fun execute() {
        for (requiredFolder in requiredFolders) {
            if (requiredFolder.toFile().exists()) continue

            FileUtils.createDirectory(requiredFolder)
        }
    }
}