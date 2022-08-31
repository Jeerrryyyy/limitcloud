package com.jevzo.limitcloud.library.commands

import com.jevzo.limitcloud.library.threading.ThreadPool
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.BufferedReader
import java.io.InputStreamReader

class CommandManager(
    private val threadPool: ThreadPool
) {

    private val logger: Logger = LoggerFactory.getLogger(CommandManager::class.java)
    private var enabled = false

    val commands: MutableList<Command> = mutableListOf()

    fun start() {
        enabled = true

        threadPool.internalPool.threadFactory.newThread {
            val bufferedReader = BufferedReader(InputStreamReader(System.`in`))

            while (enabled) {
                val args = bufferedReader.readLine().split(" ").toTypedArray()
                val command = this.parseCommand(args)

                if (command == null) {
                    logger.warn("Command not found. Please try \"help\"")
                    continue
                }

                val executed = command.execute(args.copyOfRange(1, args.size))

                if (!executed) {
                    logger.warn("Something went wrong while executing the command! Please try \"help\"")
                }
            }
        }.start()
    }

    fun stop() {
        enabled = false
    }

    fun registerCommand(command: Command) {
        if (!command::class.java.isAnnotationPresent(CommandInformation::class.java)) return
        commands.add(command)
    }

    private fun parseCommand(args: Array<String>): Command? {
        val commandName = args[0]

        if (commandName == "") return null

        for (command in commands) {
            if (!command::class.java.isAnnotationPresent(CommandInformation::class.java)) continue
            val commandInformation = command::class.java.getAnnotation(CommandInformation::class.java)

            if (commandName != commandInformation.command) {
                if (!commandInformation.aliases.contains(commandName)) continue
            }

            if ((commandName == commandInformation.command) || commandInformation.aliases.contains(commandName))
                return command
        }

        return null
    }
}