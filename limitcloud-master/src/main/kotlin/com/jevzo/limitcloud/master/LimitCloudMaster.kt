package com.jevzo.limitcloud.master

import com.google.gson.GsonBuilder
import com.jevzo.limitcloud.library.commands.CommandManager
import com.jevzo.limitcloud.library.configuration.ConfigurationExecutor
import com.jevzo.limitcloud.library.network.error.ErrorHandler
import com.jevzo.limitcloud.library.network.helper.NettyHelper
import com.jevzo.limitcloud.library.network.protocol.PacketId
import com.jevzo.limitcloud.library.network.protocol.PacketRegistry
import com.jevzo.limitcloud.library.threading.ThreadPool
import com.jevzo.limitcloud.master.commands.HelpCommand
import com.jevzo.limitcloud.master.commands.UpdateBungeeGroups
import com.jevzo.limitcloud.master.commands.UpdateSpigotGroups
import com.jevzo.limitcloud.master.configuration.*
import com.jevzo.limitcloud.master.groups.bungee.BungeeGroupHandler
import com.jevzo.limitcloud.master.groups.spigot.SpigotGroupHandler
import com.jevzo.limitcloud.master.network.NetworkServer
import com.jevzo.limitcloud.master.network.utils.NetworkUtils
import com.jevzo.limitcloud.master.network.web.WebServer
import com.jevzo.limitcloud.master.network.web.router.Router
import com.jevzo.limitcloud.master.network.web.router.routes.MasterStatusRoute
import com.jevzo.limitcloud.master.process.handler.handlers.BungeeProcessRequestHandler
import com.jevzo.limitcloud.master.process.handler.handlers.SpigotProcessRequestHandler
import com.jevzo.limitcloud.master.process.protocol.out.PacketOutRequestBungeeProcess
import com.jevzo.limitcloud.master.process.protocol.out.PacketOutRequestSpigotProcess
import com.jevzo.limitcloud.master.process.registry.registries.BungeeProcessRegistry
import com.jevzo.limitcloud.master.process.registry.registries.SpigotProcessRegistry
import com.jevzo.limitcloud.master.runtime.DirectoryConstants
import com.jevzo.limitcloud.master.runtime.RuntimeVars
import com.jevzo.limitcloud.master.slave.SlaveRegistry
import com.jevzo.limitcloud.master.slave.protocol.`in`.PacketInSlaveRequestConnection
import com.jevzo.limitcloud.master.slave.protocol.`in`.PacketInSlaveUpdateLoadStatus
import com.jevzo.limitcloud.master.slave.protocol.out.PacketOutSlaveConnectionEstablished
import org.kodein.di.DI
import org.kodein.di.bindSingleton
import org.kodein.di.direct
import org.kodein.di.instance
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.system.exitProcess

class LimitCloudMaster {

    private val logger: Logger = LoggerFactory.getLogger(LimitCloudMaster::class.java)

    companion object {
        lateinit var KODEIN: DI
    }

    fun start(args: Array<String>) {
        this.initializeDI()
        this.checkForRoot(args)
        this.checkForDebug(args)

        KODEIN.direct.instance<ConfigurationExecutor>().executeConfigurations()
        KODEIN.direct.instance<CommandManager>().start()
        KODEIN.direct.instance<NetworkServer>().startServer(KODEIN.direct.instance<RuntimeVars>().masterConfig.masterPort)
        KODEIN.direct.instance<WebServer>().startServer(KODEIN.direct.instance<RuntimeVars>().masterConfig.webServerPort)
    }

    fun shutdownGracefully() {
        KODEIN.direct.instance<CommandManager>().stop()
        KODEIN.direct.instance<NetworkServer>().shutdownGracefully()
        KODEIN.direct.instance<WebServer>().shutdownGracefully()
        KODEIN.direct.instance<ThreadPool>().shutdown()

        logger.info("Thank you for using LimitCloud!")
    }

    private fun initializeDI() {
        KODEIN = DI {
            bindSingleton { this@LimitCloudMaster }

            bindSingleton { ThreadPool() }
            bindSingleton { GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create() }

            bindSingleton { DirectoryConstants() }
            bindSingleton { RuntimeVars() }
            bindSingleton { NettyHelper() }
            bindSingleton { NetworkUtils() }

            bindSingleton { SpigotGroupHandler(instance()) }
            bindSingleton { BungeeGroupHandler(instance()) }

            bindSingleton {
                val configurationExecutor = ConfigurationExecutor()

                configurationExecutor.registerConfiguration(DefaultFolderCreator(instance()))
                configurationExecutor.registerConfiguration(DefaultCloudConfiguration(instance(), instance()))
                configurationExecutor.registerConfiguration(SpigotDownloadConfiguration(instance()))
                configurationExecutor.registerConfiguration(BungeeDownloadConfiguration(instance()))
                configurationExecutor.registerConfiguration(KeysCreator(instance(), instance()))
                configurationExecutor.registerConfiguration(SpigotGroupLoader(instance(), instance()))
                configurationExecutor.registerConfiguration(BungeeGroupLoader(instance(), instance()))

                configurationExecutor
            }

            bindSingleton { SlaveRegistry() }

            bindSingleton { SpigotProcessRegistry() }
            bindSingleton { BungeeProcessRegistry() }

            bindSingleton { SpigotProcessRequestHandler(instance(), instance(), instance(), instance()) }
            bindSingleton { BungeeProcessRequestHandler(instance(), instance(), instance(), instance()) }

            bindSingleton {
                val commandManager = CommandManager(instance())

                commandManager.registerCommand(HelpCommand(commandManager))
                commandManager.registerCommand(UpdateSpigotGroups(instance(), instance()))
                commandManager.registerCommand(UpdateBungeeGroups(instance(), instance()))

                commandManager
            }

            bindSingleton {
                val packetRegistry = PacketRegistry()

                packetRegistry.registerIncomingPacket(PacketId.PACKET_REQUEST_CONNECTION, PacketInSlaveRequestConnection::class.java)
                packetRegistry.registerIncomingPacket(PacketId.PACKET_UPDATE_LOAD_STATUS, PacketInSlaveUpdateLoadStatus::class.java)

                packetRegistry.registerOutgoingPacket(PacketId.PACKET_ESTABLISHED_CONNECTION, PacketOutSlaveConnectionEstablished::class.java)
                packetRegistry.registerOutgoingPacket(PacketId.PACKET_REQUEST_BUNGEE_PROCESS, PacketOutRequestBungeeProcess::class.java)
                packetRegistry.registerOutgoingPacket(PacketId.PACKET_REQUEST_SPIGOT_PROCESS, PacketOutRequestSpigotProcess::class.java)

                packetRegistry
            }

            bindSingleton { ErrorHandler() }

            bindSingleton {
                val router = Router()

                router.registerRoute("/status", MasterStatusRoute())

                router
            }

            bindSingleton { NetworkServer(instance(), instance()) }
            bindSingleton { WebServer(instance()) }
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
}