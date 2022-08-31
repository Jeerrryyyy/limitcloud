package com.jevzo.limitcloud.master.commands

import com.jevzo.limitcloud.library.commands.Command
import com.jevzo.limitcloud.library.commands.CommandInformation
import com.jevzo.limitcloud.library.commands.CommandManager
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@CommandInformation(
    command = "help",
    description = "This command shows this page!",
    aliases = ["h", "?"]
)
class HelpCommand(
    private val commandManager: CommandManager
) : Command {

    private val logger: Logger = LoggerFactory.getLogger(HelpCommand::class.java)

    override fun execute(args: Array<String>): Boolean {
        logger.info("LimitCloud - Command Help Page - Version: 0.0.1")

        commandManager.commands.forEach {
            val commandInformation = it.javaClass.getAnnotation(CommandInformation::class.java)
            logger.info("${commandInformation.command} ${commandInformation.aliases.asList()} | ${commandInformation.description}")
        }

        return true
    }
}