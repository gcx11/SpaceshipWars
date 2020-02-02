package me.gcx11.spaceshipwars.spaceship

import me.gcx11.spaceshipwars.components.ClientComponent
import me.gcx11.spaceshipwars.components.GeometricComponent
import me.gcx11.spaceshipwars.geometry.Point
import me.gcx11.spaceshipwars.models.Entity

actual object SpaceshipFactory {
    fun create(x: Float, y: Float, clientId: Long): Entity {
        return Entity().apply {
            addComponent(
                ClientComponent(
                    this,
                    clientId
                )
            )
            addComponent(GeometricComponent(this, x, y, 20f, 20f, 20f, 0f))
        }.apply {
            this.externalId = this.internalId
        }
    }
}