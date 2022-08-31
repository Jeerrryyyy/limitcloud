package com.jevzo.limitcloud.master.configuration

import com.jevzo.limitcloud.library.configuration.Configuration
import com.jevzo.limitcloud.library.document.Document
import com.jevzo.limitcloud.master.groups.spigot.SpigotGroupHandler
import com.jevzo.limitcloud.master.groups.spigot.models.SpigotGroup
import com.jevzo.limitcloud.master.runtime.DirectoryConstants
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

class SpigotGroupLoader(
    private val directoryConstants: DirectoryConstants,
    private val spigotGroupHandler: SpigotGroupHandler
) : Configuration() {

    private val logger: Logger = LoggerFactory.getLogger(SpigotGroupLoader::class.java)

    override fun execute() {
        val spigotGroupFiles = File(directoryConstants.masterConfigGroupsSpigot).listFiles()

        if (spigotGroupFiles == null || spigotGroupFiles.isEmpty()) {
            val spigotGroup = SpigotGroup(
                name = "Lobby",
                template = "default",
                maxServersOnline = 1,
                minServersOnline = 1,
                maxMemory = 128,
                minMemory = 64,
                maxPlayers = 64,
                newServerPercentage = 100,
                joinPower = 0,
                maintenance = false,
                lobbyServer = true,
                dynamicServer = false,
                staticServer = false,
                randomTemplateMode = false,
                templateModes = mutableListOf()
            )

            spigotGroupHandler.createGroup(spigotGroup)

            logger.warn("Cannot find any spigot groups to load. Created the default one!")
            return
        }

        for (spigotGroupFile in spigotGroupFiles) {
            if (!spigotGroupFile.name.endsWith(".json")) continue

            val document = Document.read(spigotGroupFile)
            val spigotGroup = SpigotGroup.fromDocument(document)

            spigotGroupHandler.registerGroup(spigotGroup)
        }
    }
}