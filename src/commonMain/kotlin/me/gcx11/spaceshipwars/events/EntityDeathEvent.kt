package me.gcx11.spaceshipwars.events

import me.gcx11.spaceshipwars.models.Entity

class EntityDeathEvent(
    val victim: Entity,
    val killer: Entity
): Event()