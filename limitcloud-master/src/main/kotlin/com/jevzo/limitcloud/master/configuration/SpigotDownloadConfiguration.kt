package com.jevzo.limitcloud.master.configuration

import com.jevzo.limitcloud.library.configuration.Configuration
import com.jevzo.limitcloud.library.utils.DownloadUtils
import com.jevzo.limitcloud.master.runtime.DirectoryConstants
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.net.SocketTimeoutException
import kotlin.system.exitProcess

class SpigotDownloadConfiguration(
    private val directoryConstants: DirectoryConstants
) : Configuration() {

    private val logger: Logger = LoggerFactory.getLogger(SpigotDownloadConfiguration::class.java)

    private val downloadLinks: MutableMap<String, String> = mutableMapOf(
        "SPIGOT_1_19_2" to "https://download.getbukkit.org/spigot/spigot-1.19.2.jar",
        "PAPER_1_19_2" to "https://api.papermc.io/v2/projects/paper/versions/1.19.2/builds/134/downloads/paper-1.19.2-134.jar"
    )

    override fun execute() {
        val spigotJar = File(directoryConstants.masterLocalSpigot, "spigot.jar")
        if (spigotJar.exists()) return

        val downloadLink = this.readSpigotVersion()

        try {
            DownloadUtils.downloadFile(
                downloadLink,
                spigotJar.path,
                20000,
                20000,
                false
            )
        } catch (e: SocketTimeoutException) {
            logger.error("Could not download the spigot version. Is the download server down?")
            logger.error("Used following download link: $downloadLink")
            exitProcess(0)
        }
    }

    private fun readSpigotVersion(): String {
        val availableVersions = downloadLinks.keys

        logger.info("Please choose a spigot version. $availableVersions")
        val input = bufferedReader.readLine()

        if (!(availableVersions.contains(input.uppercase()))) {
            return this.readSpigotVersion()
        }

        return downloadLinks[input.uppercase()]!!
    }
}