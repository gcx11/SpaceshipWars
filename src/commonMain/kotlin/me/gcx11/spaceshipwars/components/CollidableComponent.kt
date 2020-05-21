package me.gcx11.spaceshipwars.components

interface CollidableComponent : DisposableComponent {
    val collidedCollection: MutableCollection<CollidableComponent>

    fun collidesWith(collidable: CollidableComponent): Boolean {
        val selfGeo = getRequiredSibling<GeometricComponent>()
        val otherGeo = collidable.getOptionalSibling<GeometricComponent>()
        return otherGeo?.shape?.intersectsWith(selfGeo.shape) ?: false
    }

    fun addCollided(collidable: CollidableComponent) = collidedCollection.add(collidable)

    fun clearAllCollided() = collidedCollection.clear()

    fun allCollided(): Collection<CollidableComponent> = collidedCollection

    override fun dispose() = clearAllCollided()
}