package me.gcx11.spaceshipwars.spaceship

import me.gcx11.spaceshipwars.components.ClientComponent
import me.gcx11.spaceshipwars.models.Entity
import kotlin.math.PI

actual object SpaceshipFactory {
    fun create(entityId: Long, x: Float, y: Float, clientId: Long): Entity {
        return Entity(entityId).apply {
            addComponent(GeometricComponent(this, x, y, 20f, 20f, 20f, PI.toFloat() / 2f))
            addComponent(ShapeRenderableComponent(this))
            addComponent(
                ClientComponent(
                    this,
                    clientId
                )
            )
        }
    }
}