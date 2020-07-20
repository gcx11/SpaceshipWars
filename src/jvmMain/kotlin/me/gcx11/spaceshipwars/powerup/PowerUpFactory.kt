package me.gcx11.spaceshipwars.powerup

import me.gcx11.spaceshipwars.components.DefaultCollidableComponent
import me.gcx11.spaceshipwars.models.Entity

actual object PowerUpFactory {
    fun create(powerUpType: PowerUpType, x: Float, y: Float): Entity {
        return Entity().apply {
            addComponent(GeometricComponent(this, x, y))
            addComponent(PowerUpTypeComponent(this, powerUpType))
            addComponent(DefaultCollidableComponent(this))
        }.apply {
            this.externalId = this.internalId
            this.tag = "PowerUp"
        }
    }
}