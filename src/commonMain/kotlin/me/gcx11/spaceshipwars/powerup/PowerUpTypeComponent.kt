package me.gcx11.spaceshipwars.powerup

import me.gcx11.spaceshipwars.components.Component
import me.gcx11.spaceshipwars.models.Entity

class PowerUpTypeComponent(
    override val parent: Entity,
    val type: PowerUpType
) : Component