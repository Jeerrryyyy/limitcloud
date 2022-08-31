package com.jevzo.limitcloud.master.process.registry.registries

import com.jevzo.limitcloud.master.process.models.SpigotProcess
import com.jevzo.limitcloud.master.process.registry.ProcessRegistry

class SpigotProcessRegistry : ProcessRegistry<SpigotProcess>() {

    fun getSpigotProcessesToRegister(): MutableMap<String, Pair<String, Int>> {
        val returnMap: MutableMap<String, Pair<String, Int>> = mutableMapOf()

        for (cloudProcess in processes.getCacheValues()) {
            if (cloudProcess.lobbyServer) continue

            val pair = Pair(cloudProcess.ip, cloudProcess.port)
            returnMap[cloudProcess.name!!] = pair
        }

        return returnMap
    }

    fun getLobbyProcessesToRegister(): MutableMap<String, Pair<String, Int>> {
        val returnMap: MutableMap<String, Pair<String, Int>> = mutableMapOf()

        for (cloudProcess in processes.getCacheValues()) {
            if (!cloudProcess.lobbyServer) continue

            val pair = Pair(cloudProcess.ip, cloudProcess.port)
            returnMap[cloudProcess.name!!] = pair
        }

        return returnMap
    }
}