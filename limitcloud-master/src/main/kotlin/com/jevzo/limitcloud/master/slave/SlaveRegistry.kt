package com.jevzo.limitcloud.master.slave

import com.jevzo.limitcloud.library.cache.Cache
import com.jevzo.limitcloud.library.dto.SlaveInfo
import io.netty.channel.Channel
import java.util.stream.Collectors

class SlaveRegistry {

    val slaves: Cache<String, SlaveInfo> = Cache()

    fun registerSlave(slaveInfo: SlaveInfo): Boolean {
        if (slaves.containsKey(slaveInfo.uuid)) return false
        slaves[slaveInfo.uuid] = slaveInfo

        return slaves.containsKey(slaveInfo.uuid)
    }

    fun unregisterSlave(uuid: String) {
        slaves.remove(uuid)
    }

    fun updateSlave(uuid: String, slaveInfo: SlaveInfo) {
        this.unregisterSlave(uuid)
        this.registerSlave(slaveInfo)
    }

    fun getSlave(uuid: String): SlaveInfo? {
        return slaves[uuid]
    }

    fun getSlavesByGroup(group: String): MutableList<SlaveInfo> {
        return this.getSlaves().stream()
            .filter { it.responsibleGroups.contains(group) }
            .collect(Collectors.toList())
    }

    fun getSlaveByChannel(channel: Channel): SlaveInfo? {
        return this.getSlaves().stream()
            .filter { it.channel == channel }
            .findFirst()
            .orElse(null)
    }

    fun getSlaves(): MutableList<SlaveInfo> {
        return slaves.getCacheValues()
    }

    fun getLeastUsedSlave(group: String): SlaveInfo? {
        val slavesForGroup = this.getSlavesByGroup(group)

        if (slavesForGroup.isEmpty()) return null

        var bestSlave: SlaveInfo? = null

        for (slave in slavesForGroup) {
            if (bestSlave == null) {
                bestSlave = slave
                continue
            }

            if (slave.currentMemoryConsumption < bestSlave.currentMemoryConsumption
                && slave.currentCpuConsumption < bestSlave.currentCpuConsumption
            ) {
                bestSlave = slave
            }
        }

        if (bestSlave == slavesForGroup.first()) {
            for (slave in slavesForGroup) {
                if (bestSlave == null) {
                    bestSlave = slave
                    continue
                }

                if (slave.currentMemoryConsumption < bestSlave.currentMemoryConsumption) {
                    bestSlave = slave
                }
            }
        }

        return bestSlave
    }
}