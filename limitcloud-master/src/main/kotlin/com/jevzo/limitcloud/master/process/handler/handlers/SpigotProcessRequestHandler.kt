package com.jevzo.limitcloud.master.process.handler.handlers

import com.jevzo.limitcloud.library.dto.SlaveInfo
import com.jevzo.limitcloud.library.process.ProcessStage
import com.jevzo.limitcloud.library.process.ProcessType
import com.jevzo.limitcloud.library.utils.PortUtils
import com.jevzo.limitcloud.master.groups.spigot.SpigotGroupHandler
import com.jevzo.limitcloud.master.network.utils.NetworkUtils
import com.jevzo.limitcloud.master.process.handler.ProcessRequestHandler
import com.jevzo.limitcloud.master.process.models.SpigotProcess
import com.jevzo.limitcloud.master.process.protocol.out.PacketOutRequestSpigotProcess
import com.jevzo.limitcloud.master.process.registry.registries.SpigotProcessRegistry
import com.jevzo.limitcloud.master.slave.SlaveRegistry
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

class SpigotProcessRequestHandler(
    private val networkUtils: NetworkUtils,
    private val spigotProcessRegistry: SpigotProcessRegistry,
    private val slaveRegistry: SlaveRegistry,
    private val spigotGroupHandler: SpigotGroupHandler
) : ProcessRequestHandler<SpigotProcess> {

    private val logger: Logger = LoggerFactory.getLogger(SpigotProcessRequestHandler::class.java)

    override fun requestProcessesOnConnect(slaveInfo: SlaveInfo) {
        for (spigotGroup in slaveInfo.responsibleGroups.stream().filter { spigotGroupHandler.getGroup(it) != null }.map { spigotGroupHandler.getGroup(it) }) {
            if (spigotGroup == null) {
                logger.error("An error occurred while requesting the processes on connect of Slave:(Name=${slaveInfo.name}${slaveInfo.delimiter}${slaveInfo.suffix})")
                continue
            }

            val currentlyRunning = spigotProcessRegistry.getRunningProcessCount(spigotGroup.name)
            val slaveChannel = slaveInfo.channel

            if (slaveChannel == null) {
                logger.error("Somehow the slaves channel is null... Did the slave crash?")
                continue
            }

            val ip = slaveChannel.remoteAddress().toString().replace("/", "").split(":")[0]

            if (spigotGroup.minServersOnline != 1) {
                if (currentlyRunning >= spigotGroup.minServersOnline) continue

                val count = spigotGroup.minServersOnline - currentlyRunning
                val memoryUsageAfterRequest = slaveInfo.currentMemoryConsumption + (spigotGroup.maxMemory * count)

                if (memoryUsageAfterRequest > slaveInfo.memory) {
                    logger.error("Could not handle the request. It would consume too much memory!")
                    continue
                }

                this.requestMultipleProcesses(
                    count,
                    SpigotProcess(
                        groupName = spigotGroup.name,
                        name = null,
                        uuid = null,
                        ip = ip,
                        type = ProcessType.SPIGOT,
                        stage = ProcessStage.STARTING,
                        minMemory = spigotGroup.minMemory,
                        maxMemory = spigotGroup.maxMemory,
                        port = -1,
                        maxPlayers = spigotGroup.maxPlayers,
                        joinPower = spigotGroup.joinPower,
                        maintenance = spigotGroup.maintenance,
                        lobbyServer = spigotGroup.lobbyServer,
                        dynamicServer = spigotGroup.dynamicServer,
                        staticServer = spigotGroup.staticServer
                    )
                )
            } else {
                if (currentlyRunning >= spigotGroup.minServersOnline) {
                    continue
                }

                this.requestProcess(
                    SpigotProcess(
                        groupName = spigotGroup.name,
                        name = null,
                        uuid = null,
                        ip = ip,
                        type = ProcessType.SPIGOT,
                        stage = ProcessStage.STARTING,
                        minMemory = spigotGroup.minMemory,
                        maxMemory = spigotGroup.maxMemory,
                        port = -1,
                        maxPlayers = spigotGroup.maxPlayers,
                        joinPower = spigotGroup.joinPower,
                        maintenance = spigotGroup.maintenance,
                        lobbyServer = spigotGroup.lobbyServer,
                        dynamicServer = spigotGroup.dynamicServer,
                        staticServer = spigotGroup.staticServer
                    )
                )
            }
        }
    }

    override fun requestMultipleProcesses(count: Int, process: SpigotProcess) {
        (1..count).forEach { _ ->
            this.requestProcess(process)
        }
    }

    override fun requestProcess(process: SpigotProcess) {
        val groupName = process.groupName
        val currentlyRunning = spigotProcessRegistry.getRunningProcessCount(groupName)

        process.uuid = UUID.randomUUID().toString()
        process.port = PortUtils.getNextFreePort(60000)
        process.name = "$groupName-${if ((currentlyRunning + 1) >= 10) "${currentlyRunning + 1}" else "0${currentlyRunning + 1}"}"

        val slaveInfo = slaveRegistry.getLeastUsedSlave(groupName)

        if (slaveInfo == null) {
            logger.error("Could not find any usable slave for SpigotProcess:(Name=${process.name})")
            return
        }

        val slaveChannel = slaveInfo.channel

        if (slaveChannel == null) {
            logger.error("Somehow the slaves channel is null... Did the slave crash?")
            return
        }

        networkUtils.sendPacket(PacketOutRequestSpigotProcess(process), slaveChannel)

        spigotProcessRegistry.registerProcess(process)
        logger.info("Requested new SpigotProcess:(Name=${process.name}) on Slave:(Name=${slaveInfo.name}${slaveInfo.delimiter}${slaveInfo.suffix})")
    }
}