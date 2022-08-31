package com.jevzo.limitcloud.master.configuration

import com.jevzo.limitcloud.library.configuration.Configuration
import com.jevzo.limitcloud.library.document.Document
import com.jevzo.limitcloud.master.groups.bungee.BungeeGroupHandler
import com.jevzo.limitcloud.master.groups.bungee.models.BungeeGroup
import com.jevzo.limitcloud.master.runtime.DirectoryConstants
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

class BungeeGroupLoader(
    private val directoryConstants: DirectoryConstants,
    private val bungeeGroupHandler: BungeeGroupHandler
) : Configuration() {

    private val logger: Logger = LoggerFactory.getLogger(BungeeGroupLoader::class.java)

    override fun execute() {
        val bungeeGroupFiles = File(directoryConstants.masterConfigGroupsBungee).listFiles()

        if (bungeeGroupFiles == null || bungeeGroupFiles.isEmpty()) {
            val bungeeGroup = BungeeGroup(
                name = "Bungee",
                maxServersOnline = 1,
                minServersOnline = 1,
                maxMemory = 512,
                minMemory = 128,
                maxPlayers = 1000,
                joinPower = 0,
                maintenance = false
            )

            bungeeGroupHandler.createGroup(bungeeGroup)

            logger.warn("Cannot find any bungee groups to load. Created the default one!")
            return
        }

        for (bungeeGroupFile in bungeeGroupFiles) {
            if (!bungeeGroupFile.name.endsWith(".json")) continue

            val document = Document.read(bungeeGroupFile)
            val bungeeGroup = BungeeGroup.fromDocument(document)

            bungeeGroupHandler.registerGroup(bungeeGroup)
        }
    }
}