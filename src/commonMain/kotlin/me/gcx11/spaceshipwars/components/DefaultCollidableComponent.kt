package me.gcx11.spaceshipwars.components

import me.gcx11.spaceshipwars.geometry.Shape
import me.gcx11.spaceshipwars.models.Entity

open class DefaultCollidableComponent(
    override val parent: Entity,
    override val collidedCollection: MutableCollection<CollidableComponent> = mutableListOf()
) : CollidableComponent {
    override fun getShape(): Shape? {
        return parent.getRequiredComponent<GeometricComponent>().shape
    }
}