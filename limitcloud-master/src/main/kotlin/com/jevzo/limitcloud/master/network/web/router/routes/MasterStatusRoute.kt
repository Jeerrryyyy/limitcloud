package com.jevzo.limitcloud.master.network.web.router.routes

import com.google.gson.JsonArray
import com.jevzo.limitcloud.library.document.Document
import com.jevzo.limitcloud.library.threading.ThreadPool
import com.jevzo.limitcloud.library.utils.HardwareUtils
import com.jevzo.limitcloud.library.utils.RoundUtils
import com.jevzo.limitcloud.master.LimitCloudMaster
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.*
import io.netty.util.CharsetUtil
import org.kodein.di.instance
import com.jevzo.limitcloud.master.network.web.router.Route
import com.jevzo.limitcloud.master.slave.SlaveRegistry

class MasterStatusRoute : Route {

    private val slaveRegistry: SlaveRegistry by LimitCloudMaster.KODEIN.instance()
    private val threadPool: ThreadPool by LimitCloudMaster.KODEIN.instance()

    override fun handle(channelHandlerContext: ChannelHandlerContext, fullHttpRequest: FullHttpRequest): HttpResponse {
        val statusInfo = Document().appendBoolean("reachable", true)
            .appendLong("startup", HardwareUtils.getSystemStartupTime())
            .appendLong("uptime", HardwareUtils.getSystemUptime())

        val systemInfo = Document().appendDouble("cpuUsage", RoundUtils.roundDouble(HardwareUtils.getCpuUsage(), 2))
            .appendDouble("internalCpuUsage", RoundUtils.roundDouble(HardwareUtils.getInternalCpuUsage(), 2))
            .appendLong("memoryUsage", HardwareUtils.getRuntimeMemoryUsage())
            .appendInt("runningThreads", Thread.activeCount())
            .appendInt("runningExecutorServiceThreads", threadPool.internalPool.activeCount)

        val slaveInfo = JsonArray()

        slaveRegistry.getSlaves().forEach {
            val document = Document().appendString("slaveName", "${it.name}${it.delimiter}${it.suffix}")
                .appendLong("memory", it.memory)
                .appendLong("currentMemoryConsumption", it.currentMemoryConsumption / 1024 / 1024)
                .appendDouble("currentCpuConsumption", RoundUtils.roundDouble(it.currentCpuConsumption, 2))

            slaveInfo.add(document.getAsJsonObject())
        }

        val responseDocument = Document().appendString("version", "0.0.1")
            .appendDocument("statusInfo", statusInfo)
            .appendDocument("systemInfo", systemInfo)
            .appendJsonElement("slaveInfo", slaveInfo)

        val httpResponse = DefaultFullHttpResponse(
            HttpVersion.HTTP_1_1,
            HttpResponseStatus.OK,
            Unpooled.copiedBuffer("${responseDocument.getAsString()}\r\n", CharsetUtil.UTF_8)
        )

        httpResponse.headers().set("content-type", "application/json; charset=utf-8")
        return httpResponse
    }
}