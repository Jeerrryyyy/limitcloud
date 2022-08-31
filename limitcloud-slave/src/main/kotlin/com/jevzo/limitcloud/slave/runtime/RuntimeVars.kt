package com.jevzo.limitcloud.slave.runtime

import com.jevzo.limitcloud.slave.configuration.models.SlaveConfig
import io.netty.channel.Channel
import kotlin.properties.Delegates

class RuntimeVars {
    lateinit var masterChannel: Channel
    lateinit var slaveConfig: SlaveConfig
    lateinit var secretKey: String
    lateinit var webKey: String

    var debug by Delegates.notNull<Boolean>()
}