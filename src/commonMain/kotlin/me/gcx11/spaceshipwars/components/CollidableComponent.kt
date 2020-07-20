package me.gcx11.spaceshipwars.components

import me.gcx11.spaceshipwars.geometry.Shape

interface CollidableComponent : DisposableComponent {
    val collidedCollection: MutableCollection<CollidableComponent>

    fun getShape(): Shape?

    fun collidesWith(collidable: CollidableComponent): Boolean {
        val shape = getShape()
        val otherShape = collidable.getShape()

        if (shape == null || otherShape == null) return false
        return otherShape.intersectsWith(shape)
    }

    fun addCollided(collidable: CollidableComponent) = collidedCollection.add(collidable)

    fun clearAllCollided() = collidedCollection.clear()

    fun allCollided(): Collection<CollidableComponent> = collidedCollection

    override fun dispose() = clearAllCollided()
}