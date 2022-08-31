package com.jevzo.limitcloud.library.commands

interface Command {

    fun execute(args: Array<String>): Boolean
}