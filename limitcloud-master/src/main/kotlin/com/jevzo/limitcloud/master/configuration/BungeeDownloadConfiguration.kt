package com.jevzo.limitcloud.master.configuration

import com.jevzo.limitcloud.library.configuration.Configuration
import com.jevzo.limitcloud.library.utils.DownloadUtils
import com.jevzo.limitcloud.master.runtime.DirectoryConstants
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import kotlin.system.exitProcess

class BungeeDownloadConfiguration(
    private val directoryConstants: DirectoryConstants
) : Configuration() {

    private val logger: Logger = LoggerFactory.getLogger(BungeeDownloadConfiguration::class.java)

    private val downloadLinks: MutableMap<String, String> = mutableMapOf(
        "BUNGEECORD" to "https://ci.md-5.net/job/BungeeCord/lastSuccessfulBuild/artifact/bootstrap/target/BungeeCord.jar",
        "WATERFALL" to "https://api.papermc.io/v2/projects/waterfall/versions/1.19/builds/503/downloads/waterfall-1.19-503.jar"
    )

    override fun execute() {
        val bungeeJar = File(directoryConstants.masterLocalBungee, "bungeecord.jar")
        if (bungeeJar.exists()) return

        val downloadLink = this.readBungeeVersion()

        try {
            DownloadUtils.downloadFile(
                downloadLink,
                bungeeJar.path,
                20000,
                20000,
                false
            )
        } catch (e: Exception) {
            logger.error("Could not download the bungeecord version. Is the download server down?")
            logger.error("Used following download link: $downloadLink")
            exitProcess(0)
        }
    }

    private fun readBungeeVersion(): String {
        val availableVersions = downloadLinks.keys

        logger.info("Please choose a bungeecord version. $availableVersions")
        val input = bufferedReader.readLine()

        if (!(availableVersions.contains(input.uppercase()))) {
            return this.readBungeeVersion()
        }

        return downloadLinks[input.uppercase()]!!
    }
}