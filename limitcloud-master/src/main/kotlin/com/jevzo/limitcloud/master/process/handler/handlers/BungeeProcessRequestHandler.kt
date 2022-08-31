package com.jevzo.limitcloud.master.process.handler.handlers

import com.jevzo.limitcloud.library.dto.SlaveInfo
import com.jevzo.limitcloud.library.process.ProcessStage
import com.jevzo.limitcloud.library.process.ProcessType
import com.jevzo.limitcloud.library.utils.PortUtils
import com.jevzo.limitcloud.master.groups.bungee.BungeeGroupHandler
import com.jevzo.limitcloud.master.network.utils.NetworkUtils
import com.jevzo.limitcloud.master.process.handler.ProcessRequestHandler
import com.jevzo.limitcloud.master.process.models.BungeeProcess
import com.jevzo.limitcloud.master.process.protocol.out.PacketOutRequestBungeeProcess
import com.jevzo.limitcloud.master.process.registry.registries.BungeeProcessRegistry
import com.jevzo.limitcloud.master.slave.SlaveRegistry
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

class BungeeProcessRequestHandler(
    private val networkUtils: NetworkUtils,
    private val bungeeProcessRegistry: BungeeProcessRegistry,
    private val slaveRegistry: SlaveRegistry,
    private val bungeeGroupHandler: BungeeGroupHandler
) : ProcessRequestHandler<BungeeProcess> {

    private val logger: Logger = LoggerFactory.getLogger(BungeeProcessRequestHandler::class.java)

    override fun requestProcessesOnConnect(slaveInfo: SlaveInfo) {
        for (bungeeGroup in slaveInfo.responsibleGroups.stream().filter { bungeeGroupHandler.getGroup(it) != null }.map { bungeeGroupHandler.getGroup(it) }) {
            if (bungeeGroup == null) {
                logger.error("An error occurred while requesting the processes on connect of Slave:(Name=${slaveInfo.name}${slaveInfo.delimiter}${slaveInfo.suffix})")
                continue
            }

            val currentlyRunning = bungeeProcessRegistry.getRunningProcessCount(bungeeGroup.name)
            val slaveChannel = slaveInfo.channel

            if (slaveChannel == null) {
                logger.error("Somehow the slaves channel is null... Did the slave crash?")
                continue
            }

            val ip = slaveChannel.remoteAddress().toString().replace("/", "").split(":")[0]

            if (bungeeGroup.minServersOnline != 1) {
                if (currentlyRunning >= bungeeGroup.minServersOnline) continue

                val count = bungeeGroup.minServersOnline - currentlyRunning
                val memoryUsageAfterRequest = slaveInfo.currentMemoryConsumption + (bungeeGroup.maxMemory * count)

                if (memoryUsageAfterRequest > slaveInfo.memory) {
                    logger.error("Could not handle the request. It would consume too much memory!")
                    continue
                }

                this.requestMultipleProcesses(
                    count,
                    BungeeProcess(
                        groupName = bungeeGroup.name,
                        name = null,
                        uuid = null,
                        ip = ip,
                        type = ProcessType.BUNGEE,
                        stage = ProcessStage.STARTING,
                        minMemory = bungeeGroup.minMemory,
                        maxMemory = bungeeGroup.maxMemory,
                        port = -1,
                        maxPlayers = bungeeGroup.maxPlayers,
                        joinPower = bungeeGroup.joinPower,
                        maintenance = bungeeGroup.maintenance
                    )
                )
            } else {
                if (currentlyRunning >= bungeeGroup.minServersOnline) {
                    continue
                }

                this.requestProcess(
                    BungeeProcess(
                        groupName = bungeeGroup.name,
                        name = null,
                        uuid = null,
                        ip = ip,
                        type = ProcessType.BUNGEE,
                        stage = ProcessStage.STARTING,
                        minMemory = bungeeGroup.minMemory,
                        maxMemory = bungeeGroup.maxMemory,
                        port = -1,
                        maxPlayers = bungeeGroup.maxPlayers,
                        joinPower = bungeeGroup.joinPower,
                        maintenance = bungeeGroup.maintenance
                    )
                )
            }
        }
    }

    override fun requestMultipleProcesses(count: Int, process: BungeeProcess) {
        (1..count).forEach { _ ->
            this.requestProcess(process)
        }
    }

    override fun requestProcess(process: BungeeProcess) {
        val groupName = process.groupName
        val currentlyRunning = bungeeProcessRegistry.getRunningProcessCount(groupName)

        process.uuid = UUID.randomUUID().toString()
        process.port = PortUtils.getNextFreePort(25565)
        process.name = "$groupName-${if ((currentlyRunning + 1) >= 10) "${currentlyRunning + 1}" else "0${currentlyRunning + 1}"}"

        val slaveInfo = slaveRegistry.getLeastUsedSlave(groupName)

        if (slaveInfo == null) {
            logger.error("Could not find any usable slave for BungeeProcess:(Name=${process.name})")
            return
        }

        val slaveChannel = slaveInfo.channel

        if (slaveChannel == null) {
            logger.error("Somehow the slaves channel is null... Did the slave crash?")
            return
        }

        networkUtils.sendPacket(PacketOutRequestBungeeProcess(process), slaveChannel)

        bungeeProcessRegistry.registerProcess(process)
        logger.info("Requested new BungeeProcess:(Name=${process.name}) on Slave:(Name=${slaveInfo.name}${slaveInfo.delimiter}${slaveInfo.suffix})")
    }
}