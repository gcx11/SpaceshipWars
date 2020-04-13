package me.gcx11.spaceshipwars.events

import me.gcx11.spaceshipwars.models.Entity

data class RemoveEntityEvent(val entity: Entity): Event()