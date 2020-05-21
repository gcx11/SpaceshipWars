package me.gcx11.spaceshipwars.events

import me.gcx11.spaceshipwars.models.Entity

class CollisionEvent(
    val firstEntity: Entity,
    val secondEntity: Entity
): Event()