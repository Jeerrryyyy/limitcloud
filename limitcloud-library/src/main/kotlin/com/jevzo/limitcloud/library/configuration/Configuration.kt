package com.jevzo.limitcloud.library.configuration

import java.io.BufferedReader
import java.io.InputStreamReader

abstract class Configuration {

    val bufferedReader: BufferedReader = BufferedReader(InputStreamReader(System.`in`))

    abstract fun execute()
}