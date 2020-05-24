package me.gcx11.spaceshipwars.time

import kotlin.js.Date

actual fun getUnixTimeMillis(): Long {
    return Date().getTime().toLong()
}