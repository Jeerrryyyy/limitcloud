package com.jevzo.limitcloud.master.groups.spigot

import com.jevzo.limitcloud.library.cache.Cache
import com.jevzo.limitcloud.library.utils.FileUtils
import com.jevzo.limitcloud.library.utils.ZipUtils
import com.jevzo.limitcloud.master.groups.spigot.models.SpigotGroup
import com.jevzo.limitcloud.master.runtime.DirectoryConstants
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Path

class SpigotGroupHandler(
    private val directoryConstants: DirectoryConstants
) {

    private val logger: Logger = LoggerFactory.getLogger(SpigotGroupHandler::class.java)

    private val spigotGroups: Cache<String, SpigotGroup> = Cache()

    fun registerGroup(spigotGroup: SpigotGroup) {
        if (spigotGroups.containsKey(spigotGroup.name)) return

        spigotGroups[spigotGroup.name] = spigotGroup
        logger.info("Successfully registered SpigotGroup:(Name=${spigotGroup.name})")
    }

    fun createGroup(spigotGroup: SpigotGroup) {
        val document = SpigotGroup.toDocument(spigotGroup)
        document.write(File(directoryConstants.masterConfigGroupsSpigot, "${spigotGroup.name}.json"))

        val templatePath = Path.of("${directoryConstants.masterTemplateSpigot}/${spigotGroup.name}")
        val defaultTemplatePath = Path.of("${directoryConstants.masterTemplateSpigot}/${spigotGroup.name}/default/")

        FileUtils.createDirectory(templatePath)
        FileUtils.createDirectory(defaultTemplatePath)
        FileUtils.createDirectory(File(defaultTemplatePath.toFile(), "plugins").toPath())

        FileUtils.copyFile(File(directoryConstants.masterLocalSpigot, "spigot.jar"), File(defaultTemplatePath.toFile(), "spigot.jar"))
        FileUtils.copyAllFiles(File(directoryConstants.masterGlobalSpigot).toPath(), File(defaultTemplatePath.toFile(), "plugins").path)

        FileUtils.copyAllFiles(templatePath, "${directoryConstants.masterWeb}/${spigotGroup.name}")
        ZipUtils.zipFiles(
            File("${directoryConstants.masterWeb}/${spigotGroup.name}"),
            File("${directoryConstants.masterWeb}/${spigotGroup.name}.zip")
        )
        FileUtils.deleteFullDirectory("${directoryConstants.masterWeb}/${spigotGroup.name}")

        spigotGroups[spigotGroup.name] = spigotGroup
        logger.info("Created and loaded SpigotGroup:(Name=${spigotGroup.name})")
    }

    fun deleteGroup(spigotGroup: SpigotGroup) {
        spigotGroups.remove(spigotGroup.name)
        FileUtils.deleteIfExists(File(directoryConstants.masterConfigGroupsSpigot, "${spigotGroup.name}.json"))
        logger.info("Deleted and unloaded SpigotGroup:(Name=${spigotGroup.name})")
    }

    fun editGroup(spigotGroup: SpigotGroup) {
        this.deleteGroup(spigotGroup)
        this.createGroup(spigotGroup)
    }

    fun getGroup(name: String): SpigotGroup? {
        return spigotGroups[name]
    }

    fun getGroups(): MutableList<SpigotGroup> {
        return spigotGroups.getCacheValues()
    }
}