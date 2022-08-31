package com.jevzo.limitcloud.slave.process

import com.jevzo.limitcloud.library.cache.Cache
import com.jevzo.limitcloud.library.threading.ThreadPool
import com.jevzo.limitcloud.library.utils.FileUtils
import com.jevzo.limitcloud.slave.process.models.CloudProcess
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.stream.Collectors

class ProcessRegistry(
    private val threadPool: ThreadPool
) {

    private val logger: Logger = LoggerFactory.getLogger(ProcessRegistry::class.java)
    private val processes: Cache<String, CloudProcess> = Cache()

    fun registerProcess(cloudProcess: CloudProcess) {
        if (processes.containsKey(cloudProcess.uuid)) return

        processes[cloudProcess.uuid] = cloudProcess
    }

    fun unregisterProcess(cloudProcess: CloudProcess) {
        processes.remove(cloudProcess.uuid)
    }

    fun updateProcess(cloudProcess: CloudProcess) {
        this.unregisterProcess(cloudProcess)
        this.registerProcess(cloudProcess)
    }

    fun getProcess(uuid: String): CloudProcess? {
        return processes[uuid]
    }

    fun getRunningProcessCount(processName: String): Int {
        return processes.getCacheValues().stream().filter { it.name.startsWith(processName) }.collect(Collectors.toList()).size
    }

    fun getTotalMemoryUsage(): Int {
        return processes.getCacheValues().stream().mapToInt { it.maxMemory }.sum()
    }

    fun killAllProcesses() {
        processes.getCacheValues().forEach {
            threadPool.internalPool.execute {
                it.process.destroy()

                var attempts = 1
                while (it.process.isAlive) {
                    logger.info("Try to kill CloudProcess:(Name=${it.name})... $attempts/10")

                    Thread.sleep(5000)

                    if (attempts == 10) {
                        it.process.destroyForcibly()
                        logger.warn("Needed to forcibly kill CloudProcess:(Name=${it.name})")
                    }

                    attempts++
                }

                FileUtils.deleteFullDirectory(it.serverDirectoryPath)
            }
        }
    }
}