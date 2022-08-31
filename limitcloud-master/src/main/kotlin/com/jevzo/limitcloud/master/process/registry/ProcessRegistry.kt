package com.jevzo.limitcloud.master.process.registry

import com.jevzo.limitcloud.library.cache.Cache
import com.jevzo.limitcloud.master.process.models.AbstractProcess
import java.util.stream.Collectors

open class ProcessRegistry<T : AbstractProcess> {

    val processes: Cache<String, T> = Cache()

    fun registerProcess(process: T) {
        if (processes.containsKey(process.uuid)) return

        processes[process.uuid] = process
    }

    fun unregisterProcess(process: T) {
        processes.remove(process.uuid)
    }

    fun updateProcess(process: T) {
        this.unregisterProcess(process)
        this.registerProcess(process)
    }

    fun getProcess(uuid: String): T? {
        return processes[uuid]
    }

    fun getRunningProcessCount(name: String): Int {
        return processes.getCacheValues().stream().filter { it.name.startsWith(name) }.collect(Collectors.toList()).size
    }
}