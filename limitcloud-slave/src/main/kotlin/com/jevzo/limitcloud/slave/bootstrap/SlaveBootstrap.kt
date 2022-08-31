package com.jevzo.limitcloud.slave.bootstrap

import com.jevzo.limitcloud.slave.LimitCloudSlave

fun main(args: Array<String>) {
    Thread.currentThread().name = "limitcloud-${Thread.currentThread().id}"

    val limitCloudSlave = LimitCloudSlave()
    limitCloudSlave.start(args)

    Runtime.getRuntime().addShutdownHook(Thread({
        limitCloudSlave.shutdownGracefully()
    }, "limitcloud-shutdown"))
}