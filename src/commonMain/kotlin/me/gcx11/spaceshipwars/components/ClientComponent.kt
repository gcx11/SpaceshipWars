package me.gcx11.spaceshipwars.components

import me.gcx11.spaceshipwars.models.Entity

class ClientComponent(
    override val parent: Entity,
    val clientId: Long
): Component