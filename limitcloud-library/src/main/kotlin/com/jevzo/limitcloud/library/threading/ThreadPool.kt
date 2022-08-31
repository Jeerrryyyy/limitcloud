package com.jevzo.limitcloud.library.threading

import java.util.concurrent.Executors
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

class ThreadPool {

    val internalPool: ThreadPoolExecutor =
        Executors.newFixedThreadPool(16, InternalThreadFactory("cloudsystem")) as ThreadPoolExecutor

    fun shutdown() {
        internalPool.shutdown()

        try {
            if (!internalPool.awaitTermination(10, TimeUnit.SECONDS)) {
                internalPool.shutdownNow()
            }
        } catch (e: InterruptedException) {
            internalPool.shutdownNow()
        }
    }
}