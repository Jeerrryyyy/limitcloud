package com.jevzo.limitcloud.slave.tasks

import com.jevzo.limitcloud.library.utils.HardwareUtils
import com.jevzo.limitcloud.slave.LimitCloudSlave
import com.jevzo.limitcloud.slave.network.protocol.out.PacketOutSlaveUpdateLoadStatus
import com.jevzo.limitcloud.slave.network.utils.NetworkUtils
import com.jevzo.limitcloud.slave.runtime.RuntimeVars
import org.kodein.di.instance
import java.util.*

class UpdateLoadInfoTask : TimerTask() {

    private val runtimeVars: RuntimeVars by LimitCloudSlave.KODEIN.instance()
    private val networkUtils: NetworkUtils by LimitCloudSlave.KODEIN.instance()

    override fun run() {
        networkUtils.sendPacket(
            PacketOutSlaveUpdateLoadStatus(
                uuid = runtimeVars.slaveConfig.uuid,
                currentOnlineServers = 0, // TODO: GET FROM PROCESS REGISTRY
                currentMemoryConsumption = HardwareUtils.getRuntimeMemoryUsage(),
                currentCpuConsumption = HardwareUtils.getInternalCpuUsage(),
            ),
            runtimeVars.masterChannel
        )
    }
}