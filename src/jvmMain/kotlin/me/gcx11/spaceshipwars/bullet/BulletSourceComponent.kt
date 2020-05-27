package me.gcx11.spaceshipwars.bullet

import me.gcx11.spaceshipwars.components.Component
import me.gcx11.spaceshipwars.models.Entity

class BulletSourceComponent(
    override val parent: Entity,
    val source: Entity
): Component