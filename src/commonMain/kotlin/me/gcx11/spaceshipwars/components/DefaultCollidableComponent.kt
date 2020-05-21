package me.gcx11.spaceshipwars.components

import me.gcx11.spaceshipwars.models.Entity

class DefaultCollidableComponent(
    override val parent: Entity,
    override val collidedCollection: MutableCollection<CollidableComponent> = mutableListOf()
) : CollidableComponent