package me.gcx11.spaceshipwars.components

import me.gcx11.spaceshipwars.models.Entity

class DefaultDamagingComponent(
    override val parent: Entity,
    override val damage: Int
) : DamagingComponent