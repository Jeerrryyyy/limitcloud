package com.jevzo.limitcloud.slave.process.handler

import com.jevzo.limitcloud.library.process.ProcessStage
import com.jevzo.limitcloud.library.process.ProcessStreamConsumer
import com.jevzo.limitcloud.library.process.ProcessType
import com.jevzo.limitcloud.library.properties.ServerProperties
import com.jevzo.limitcloud.library.utils.DownloadUtils
import com.jevzo.limitcloud.library.utils.FileUtils
import com.jevzo.limitcloud.library.utils.ZipUtils
import com.jevzo.limitcloud.slave.process.ProcessRegistry
import com.jevzo.limitcloud.slave.process.models.CloudProcess
import com.jevzo.limitcloud.slave.runtime.DirectoryConstants
import com.jevzo.limitcloud.slave.runtime.RuntimeVars
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Path

class SpigotProcessRequestHandler(
    private val directoryConstants: DirectoryConstants,
    private val runtimeVars: RuntimeVars,
    private val processRegistry: ProcessRegistry
) {

    private val logger: Logger = LoggerFactory.getLogger(SpigotProcessRequestHandler::class.java)

    fun handle(
        groupName: String,
        name: String,
        uuid: String,
        type: ProcessType,
        stage: ProcessStage,
        port: Int,
        maxPlayers: Int,
        minMemory: Int,
        maxMemory: Int
    ) {
        val downloadUrl =
            "http://${runtimeVars.slaveConfig.masterAddress}:${runtimeVars.slaveConfig.webPort}/app/master/web/$groupName.zip"
        val rightPath = directoryConstants.slaveCachedTemplatesSpigot

        DownloadUtils.downloadFile(downloadUrl, "$rightPath/$groupName.zip", runtimeVars.webKey)

        val serverDirectoryPath = Path.of("${directoryConstants.slaveRunning}/${name}_$uuid")
        FileUtils.createDirectory(serverDirectoryPath)

        FileUtils.copyFile(
            File(directoryConstants.slaveCachedTemplatesSpigot, "$groupName.zip"),
            File(serverDirectoryPath.toFile(), "$groupName.zip")
        )
        ZipUtils.unzipFiles(
            File(serverDirectoryPath.toFile(), "$groupName.zip"),
            serverDirectoryPath.toFile()
        )
        FileUtils.copyAllFiles(
            File(serverDirectoryPath.toFile(), "default").toPath(),
            serverDirectoryPath.toFile().path
        )

        FileUtils.createDirectory(File(serverDirectoryPath.toFile(), "plugins/LimitCloudApi").toPath())
        FileUtils.createDirectory(File(serverDirectoryPath.toFile(), "cache").toPath())
        FileUtils.createDirectory(File(serverDirectoryPath.toFile(), "versions").toPath())
        FileUtils.createDirectory(File(serverDirectoryPath.toFile(), "versions/1.19.2").toPath()) // TODO: Get rid of these shady versions

        FileUtils.copyFile(
            File(directoryConstants.slaveCachedStatic, "mojang_1.19.2.jar"),
            File(serverDirectoryPath.toFile(), "cache/mojang_1.19.2.jar")
        )
        FileUtils.copyFile(
            File(directoryConstants.slaveCachedStatic, "paper-1.19.2.jar"),
            File(serverDirectoryPath.toFile(), "versions/1.19.2/paper-1.19.2.jar")
        )

        // TODO: Write Config File for Api

        FileUtils.writeStringToFile(File(serverDirectoryPath.toFile(), "eula.txt"), "eula=true")
        FileUtils.writeStringToFile(
            File(serverDirectoryPath.toFile(), "server.properties"),
            ServerProperties.getProperties(serverPort = port, maxPlayers = maxPlayers)
        )
        FileUtils.deleteFullDirectory(File(serverDirectoryPath.toFile(), "default").toPath())
        FileUtils.deleteIfExists(File(serverDirectoryPath.toFile(), "$groupName.zip").toPath())

        val processBuilder = ProcessBuilder()
        processBuilder.command("java","-Xms${minMemory}m", "-Xmx${maxMemory}m", "-jar", "spigot.jar", "--nogui")
        processBuilder.directory(serverDirectoryPath.toFile())

        val process = processBuilder.start()
        val processStreamConsumer = ProcessStreamConsumer(process.inputStream, System.out::println)

        val cloudProcess = CloudProcess(
            name = name,
            uuid = uuid,
            type = type,
            stage = stage,
            minMemory = minMemory,
            maxMemory = maxMemory,
            serverDirectoryPath = serverDirectoryPath,
            process = process,
            processStreamConsumer = processStreamConsumer
        )
        processRegistry.registerProcess(cloudProcess)

        Thread(processStreamConsumer).start()

        logger.info("Prepared and started new CloudProcess:(Name=${cloudProcess.name})")
    }
}