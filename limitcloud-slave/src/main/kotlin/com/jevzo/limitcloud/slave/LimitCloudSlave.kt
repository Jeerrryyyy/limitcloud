package com.jevzo.limitcloud.slave

import com.google.gson.GsonBuilder
import com.jevzo.limitcloud.library.commands.CommandManager
import com.jevzo.limitcloud.library.configuration.ConfigurationExecutor
import com.jevzo.limitcloud.library.network.helper.NettyHelper
import com.jevzo.limitcloud.library.network.protocol.PacketId
import com.jevzo.limitcloud.library.network.protocol.PacketRegistry
import com.jevzo.limitcloud.library.threading.ThreadPool
import com.jevzo.limitcloud.slave.commands.HelpCommand
import com.jevzo.limitcloud.slave.configuration.DefaultCloudConfiguration
import com.jevzo.limitcloud.slave.configuration.DefaultFolderCreator
import com.jevzo.limitcloud.slave.configuration.SlaveKeyReader
import com.jevzo.limitcloud.slave.network.NetworkClient
import com.jevzo.limitcloud.slave.network.protocol.`in`.PacketInSlaveConnectionEstablished
import com.jevzo.limitcloud.slave.network.protocol.out.PacketOutSlaveRequestConnection
import com.jevzo.limitcloud.slave.network.protocol.out.PacketOutSlaveUpdateLoadStatus
import com.jevzo.limitcloud.slave.network.utils.NetworkUtils
import com.jevzo.limitcloud.slave.process.protocol.`in`.PacketInRequestBungeeProcess
import com.jevzo.limitcloud.slave.process.protocol.`in`.PacketInRequestSpigotProcess
import com.jevzo.limitcloud.slave.runtime.DirectoryConstants
import com.jevzo.limitcloud.slave.runtime.RuntimeVars
import com.jevzo.limitcloud.slave.tasks.UpdateLoadInfoTask
import org.kodein.di.DI
import org.kodein.di.bindSingleton
import org.kodein.di.direct
import org.kodein.di.instance
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess

class LimitCloudSlave {

    private val logger: Logger = LoggerFactory.getLogger(LimitCloudSlave::class.java)

    companion object {
        lateinit var KODEIN: DI
    }

    fun start(args: Array<String>) {
        this.initializeDI()
        this.checkForRoot(args)
        this.checkForDebug(args)

        KODEIN.direct.instance<ConfigurationExecutor>().executeConfigurations()
        KODEIN.direct.instance<CommandManager>().start()

        KODEIN.direct.instance<NetworkClient>().startClient(
            KODEIN.direct.instance<RuntimeVars>().slaveConfig.masterAddress,
            KODEIN.direct.instance<RuntimeVars>().slaveConfig.masterPort
        )

        this.startTasks()
    }

    fun shutdownGracefully() {
        KODEIN.direct.instance<CommandManager>().stop()
        KODEIN.direct.instance<NetworkClient>().shutdownGracefully()
        KODEIN.direct.instance<ThreadPool>().shutdown()

        logger.info("Thank you for using LimitCloud!")
    }

    private fun initializeDI() {
        KODEIN = DI {
            bindSingleton { this@LimitCloudSlave }

            bindSingleton { ThreadPool() }
            bindSingleton { GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create() }

            bindSingleton { DirectoryConstants() }
            bindSingleton { RuntimeVars() }
            bindSingleton { NettyHelper() }
            bindSingleton { NetworkUtils() }

            bindSingleton {
                val configurationExecutor = ConfigurationExecutor()

                configurationExecutor.registerConfiguration(DefaultFolderCreator(instance()))
                configurationExecutor.registerConfiguration(DefaultCloudConfiguration(instance(), instance()))
                configurationExecutor.registerConfiguration(SlaveKeyReader(instance(), instance()))

                configurationExecutor
            }

            bindSingleton {
                val commandManager = CommandManager(instance())

                commandManager.registerCommand(HelpCommand(commandManager))

                commandManager
            }

            bindSingleton {
                val packetRegistry = PacketRegistry()

                packetRegistry.registerOutgoingPacket(PacketId.PACKET_REQUEST_CONNECTION, PacketOutSlaveRequestConnection::class.java)
                packetRegistry.registerOutgoingPacket(PacketId.PACKET_UPDATE_LOAD_STATUS, PacketOutSlaveUpdateLoadStatus::class.java)

                packetRegistry.registerIncomingPacket(PacketId.PACKET_ESTABLISHED_CONNECTION, PacketInSlaveConnectionEstablished::class.java)
                packetRegistry.registerIncomingPacket(PacketId.PACKET_REQUEST_BUNGEE_PROCESS, PacketInRequestBungeeProcess::class.java)
                packetRegistry.registerIncomingPacket(PacketId.PACKET_REQUEST_SPIGOT_PROCESS, PacketInRequestSpigotProcess::class.java)

                packetRegistry
            }

            bindSingleton { NetworkClient(instance(), instance()) }
        }
    }

    private fun checkForRoot(args: Array<String>) {
        if (System.getProperty("user.name") == "root" && !args.contains("--enable-root")) {
            logger.error("Please consider not to use the \"root\" user for security reasons!")
            logger.error("If you want to use it anyway, at your own risk, add \"--enable-root\" to the start arguments.")
            exitProcess(0)
        }
    }

    private fun checkForDebug(args: Array<String>) {
        KODEIN.direct.instance<RuntimeVars>().debug = args.contains("--debug")
    }

    private fun startTasks() {
        val timer = Timer("limitcloud-timer")
        timer.scheduleAtFixedRate(UpdateLoadInfoTask(), TimeUnit.SECONDS.toMillis(5), TimeUnit.SECONDS.toMillis(5))
    }
}