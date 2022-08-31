package com.jevzo.limitcloud.master.groups.bungee

import com.jevzo.limitcloud.library.cache.Cache
import com.jevzo.limitcloud.library.utils.FileUtils
import com.jevzo.limitcloud.library.utils.ZipUtils
import com.jevzo.limitcloud.master.groups.bungee.models.BungeeGroup
import com.jevzo.limitcloud.master.runtime.DirectoryConstants
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Path

class BungeeGroupHandler(
    private val directoryConstants: DirectoryConstants
) {

    private val logger: Logger = LoggerFactory.getLogger(BungeeGroupHandler::class.java)

    private val bungeeGroups: Cache<String, BungeeGroup> = Cache()

    fun registerGroup(bungeeGroup: BungeeGroup) {
        if (bungeeGroups.containsKey(bungeeGroup.name)) return

        bungeeGroups[bungeeGroup.name] = bungeeGroup
        logger.info("Successfully registered BungeeGroup:(Name=${bungeeGroup.name})")
    }

    fun createGroup(bungeeGroup: BungeeGroup) {
        val document = BungeeGroup.toDocument(bungeeGroup)
        document.write(File(directoryConstants.masterConfigGroupsBungee, "${bungeeGroup.name}.json"))

        bungeeGroups[bungeeGroup.name] = bungeeGroup

        val templatePath = Path.of("${directoryConstants.masterTemplateBungee}/${bungeeGroup.name}")
        val defaultTemplatePath = Path.of("${directoryConstants.masterTemplateBungee}/${bungeeGroup.name}/default/")

        FileUtils.createDirectory(templatePath)
        FileUtils.createDirectory(defaultTemplatePath)
        FileUtils.createDirectory(File(defaultTemplatePath.toFile(), "plugins").toPath())

        FileUtils.copyFile(File(directoryConstants.masterLocalBungee, "bungeecord.jar"), File(defaultTemplatePath.toFile(), "bungeecord.jar"))
        FileUtils.copyAllFiles(File(directoryConstants.masterGlobalBungee).toPath(), File(defaultTemplatePath.toFile(), "plugins").path)

        FileUtils.copyAllFiles(templatePath, "${directoryConstants.masterWeb}/${bungeeGroup.name}")
        ZipUtils.zipFiles(
            File("${directoryConstants.masterWeb}/${bungeeGroup.name}"),
            File("${directoryConstants.masterWeb}/${bungeeGroup.name}.zip")
        )
        FileUtils.deleteFullDirectory("${directoryConstants.masterWeb}/${bungeeGroup.name}")

        logger.info("Created and loaded BungeeGroup:(Name=${bungeeGroup.name})")
    }

    fun deleteGroup(bungeeGroup: BungeeGroup) {
        bungeeGroups.remove(bungeeGroup.name)
        FileUtils.deleteIfExists(File(directoryConstants.masterConfigGroupsBungee, "${bungeeGroup.name}.json"))
        logger.info("Deleted and unloaded BungeeGroup:(Name=${bungeeGroup.name})")
    }

    fun editGroup(bungeeGroup: BungeeGroup) {
        this.deleteGroup(bungeeGroup)
        this.createGroup(bungeeGroup)
    }

    fun getGroup(name: String): BungeeGroup? {
        return bungeeGroups[name]
    }

    fun getGroups(): MutableList<BungeeGroup> {
        return bungeeGroups.getCacheValues()
    }
}