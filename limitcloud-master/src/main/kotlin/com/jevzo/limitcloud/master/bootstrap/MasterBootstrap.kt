package com.jevzo.limitcloud.master.bootstrap

import com.jevzo.limitcloud.master.LimitCloudMaster

fun main(args: Array<String>) {
    Thread.currentThread().name = "limitcloud-${Thread.currentThread().id}"

    val limitCloudMaster = LimitCloudMaster()
    limitCloudMaster.start(args)

    Runtime.getRuntime().addShutdownHook(Thread({
        limitCloudMaster.shutdownGracefully()
    }, "limitcloud-shutdown"))
}