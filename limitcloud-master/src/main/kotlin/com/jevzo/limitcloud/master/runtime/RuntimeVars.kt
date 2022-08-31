package com.jevzo.limitcloud.master.runtime

import com.jevzo.limitcloud.master.configuration.models.MasterConfig
import kotlin.properties.Delegates

class RuntimeVars {
    lateinit var masterConfig: MasterConfig
    lateinit var secretKey: String
    lateinit var webKey: String

    var debug by Delegates.notNull<Boolean>()
}