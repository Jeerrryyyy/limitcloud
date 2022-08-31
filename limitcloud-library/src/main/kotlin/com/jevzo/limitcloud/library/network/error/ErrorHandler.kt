package com.jevzo.limitcloud.library.network.error

import com.jevzo.limitcloud.library.dto.SlaveInfo
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class ErrorHandler {

    private val logger: Logger = LoggerFactory.getLogger(ErrorHandler::class.java)

    fun handleError(slaveInfo: SlaveInfo?, cause: Throwable) {
        if (slaveInfo == null) {
            this.printMessage(cause)
        } else {
            this.printMessage(slaveInfo, cause)
        }
    }

    private fun printMessage(cause: Throwable) {
        logger.error("An internal error occurred but no slave seems to be affected!")
        logger.error(cause.message)
    }

    private fun printMessage(slaveInfo: SlaveInfo, cause: Throwable) {
        val slaveName = "${slaveInfo.name}${slaveInfo.delimiter}${slaveInfo.suffix}"

        logger.error("An internal error occurred and $slaveName is affected of it!")
        logger.error(cause.message)
    }
}