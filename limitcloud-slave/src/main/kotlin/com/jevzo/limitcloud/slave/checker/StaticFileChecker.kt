package com.jevzo.limitcloud.slave.checker

import com.jevzo.limitcloud.library.utils.DownloadUtils
import com.jevzo.limitcloud.slave.runtime.DirectoryConstants
import com.jevzo.limitcloud.slave.runtime.RuntimeVars
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

class StaticFileChecker(
    private val directoryConstants: DirectoryConstants,
    private val runtimeVars: RuntimeVars
) {

    private val logger: Logger = LoggerFactory.getLogger(StaticFileChecker::class.java)

    @Deprecated("Gets deleted soon!")
    fun checkForPatchedPaperJar() {
        val patchedPaperJar = File(directoryConstants.slaveCachedStatic, "paper-1.19.2.jar")

        if(patchedPaperJar.exists()) {
            logger.info("Patched paper jar found, skipping download...")
            return
        }

        val downloadUrl =
            "http://${runtimeVars.slaveConfig.masterAddress}:${runtimeVars.slaveConfig.webPort}/app/master/web/static/paper-1.19.2.jar"

        logger.info("Downloading patched paper jar...")
        DownloadUtils.downloadFile(downloadUrl, "${directoryConstants.slaveCachedStatic}/paper-1.19.2.jar", runtimeVars.webKey)
        logger.info("Downloaded patched paper jar!")
    }

    @Deprecated("Gets deleted soon!")
    fun checkForMinecraftJar() {
        val minecraftJar = File(directoryConstants.slaveCachedStatic, "mojang_1.19.2.jar")

        if(minecraftJar.exists()) {
            logger.info("Minecraft jar found, skipping download...")
            return
        }

        val downloadUrl =
            "http://${runtimeVars.slaveConfig.masterAddress}:${runtimeVars.slaveConfig.webPort}/app/master/web/static/mojang_1.19.2.jar"

        logger.info("Downloading Minecraft jar...")
        DownloadUtils.downloadFile(downloadUrl, "${directoryConstants.slaveCachedStatic}/mojang_1.19.2.jar", runtimeVars.webKey)
        logger.info("Downloaded Minecraft jar!")
    }
}