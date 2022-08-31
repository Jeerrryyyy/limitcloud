package com.jevzo.limitcloud.library.process

import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.util.function.Consumer

class ProcessStreamConsumer(
    private val inputStream: InputStream,
    private val callback: Consumer<String>
) : Runnable {

    override fun run() {
        BufferedReader(InputStreamReader(inputStream)).lines().forEach(callback)
    }
}