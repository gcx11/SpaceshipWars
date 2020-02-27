package me.gcx11.spaceshipwars

import mu.KotlinLogging

object Logger {
    val server = KotlinLogging.logger("server")
    val client = KotlinLogging.logger("client")
}