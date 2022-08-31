package com.jevzo.limitcloud.master.commands

import com.jevzo.limitcloud.library.commands.Command
import com.jevzo.limitcloud.library.commands.CommandInformation
import com.jevzo.limitcloud.library.utils.FileUtils
import com.jevzo.limitcloud.library.utils.ZipUtils
import com.jevzo.limitcloud.master.groups.bungee.BungeeGroupHandler
import com.jevzo.limitcloud.master.runtime.DirectoryConstants
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Path

@CommandInformation(
    command = "updateBungee",
    description = "Replaces current zipped bungee files with new zipped bungee files",
    aliases = ["ub", "updateB"]
)
class UpdateBungeeGroups(
    private val directoryConstants: DirectoryConstants,
    private val bungeeGroupHandler: BungeeGroupHandler
) : Command {

    private val logger: Logger = LoggerFactory.getLogger(UpdateSpigotGroups::class.java)

    override fun execute(args: Array<String>): Boolean {
        bungeeGroupHandler.getGroups().forEach {
            logger.info("Starting update of BungeeGroup:(Name=${it.name})...")

            FileUtils.deleteIfExists(File("${directoryConstants.masterWeb}/${it.name}.zip"))

            val templatePath = Path.of("${directoryConstants.masterTemplateBungee}/${it.name}")

            FileUtils.copyAllFiles(templatePath, "${directoryConstants.masterWeb}/${it.name}")
            ZipUtils.zipFiles(
                File("${directoryConstants.masterWeb}/${it.name}"),
                File("${directoryConstants.masterWeb}/${it.name}.zip")
            )
            FileUtils.deleteFullDirectory("${directoryConstants.masterWeb}/${it.name}")

            logger.info("Finished update of BungeeGroup:(Name=${it.name})...")
        }

        return true
    }
}