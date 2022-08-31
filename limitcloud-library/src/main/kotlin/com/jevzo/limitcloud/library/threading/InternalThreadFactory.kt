package com.jevzo.limitcloud.library.threading

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.ThreadFactory

class InternalThreadFactory(
    private val threadPrefix: String
) : ThreadFactory {

    private val logger: Logger = LoggerFactory.getLogger(InternalThreadFactory::class.java)

    override fun newThread(runnable: Runnable): Thread {
        val thread = Thread(runnable)

        thread.name = "$threadPrefix-${thread.id}"
        thread.isDaemon = true

        thread.setUncaughtExceptionHandler { shadowThread, cause ->
            logger.error("Thread ${shadowThread.name} has crashed", cause)
        }

        return thread
    }
}