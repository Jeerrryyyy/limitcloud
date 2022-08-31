package com.jevzo.limitcloud.master.process.handler

import com.jevzo.limitcloud.library.dto.SlaveInfo

interface ProcessRequestHandler<T> {

    fun requestProcessesOnConnect(slaveInfo: SlaveInfo)
    fun requestProcess(process: T)
    fun requestMultipleProcesses(count: Int, process: T)
}