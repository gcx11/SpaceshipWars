package me.gcx11.spaceshipwars.models

import me.gcx11.spaceshipwars.collections.SwapQueue
import me.gcx11.spaceshipwars.events.Event

val globalEventQueue = SwapQueue<Event>()

object World {
    val entities = mutableListOf<Entity>()
}