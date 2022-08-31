package com.jevzo.limitcloud.master.configuration

import com.jevzo.limitcloud.library.configuration.Configuration
import com.jevzo.limitcloud.library.utils.FileUtils
import com.jevzo.limitcloud.master.runtime.DirectoryConstants
import java.nio.file.Path

class DefaultFolderCreator(
    directoryConstants: DirectoryConstants
) : Configuration() {

    private val requiredFolders: MutableList<Path> = mutableListOf(
        Path.of(directoryConstants.master),
        Path.of(directoryConstants.masterSecure),
        Path.of(directoryConstants.masterData),
        Path.of(directoryConstants.masterDataCloudPlayers),
        Path.of(directoryConstants.masterConfig),
        Path.of(directoryConstants.masterConfigCloud),
        Path.of(directoryConstants.masterConfigGroups),
        Path.of(directoryConstants.masterConfigGroupsBungee),
        Path.of(directoryConstants.masterConfigGroupsSpigot),
        Path.of(directoryConstants.masterConfigPermissions),
        Path.of(directoryConstants.masterAddons),
        Path.of(directoryConstants.masterTemplate),
        Path.of(directoryConstants.masterTemplateBungee),
        Path.of(directoryConstants.masterTemplateSpigot),
        Path.of(directoryConstants.masterWeb),
        Path.of(directoryConstants.masterGlobal),
        Path.of(directoryConstants.masterGlobalBungee),
        Path.of(directoryConstants.masterGlobalSpigot),
        Path.of(directoryConstants.masterLocal),
        Path.of(directoryConstants.masterLocalSpigot),
        Path.of(directoryConstants.masterLocalBungee)
    )

    override fun execute() {
        for (requiredFolder in requiredFolders) {
            if (requiredFolder.toFile().exists()) continue

            FileUtils.createDirectory(requiredFolder)
        }
    }
}