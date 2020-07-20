package me.gcx11.spaceshipwars.powerup

import me.gcx11.spaceshipwars.models.Entity

actual object PowerUpFactory {
    fun create(entityId: Long, powerUpType: PowerUpType, x: Float, y: Float): Entity {
        return Entity(entityId).apply {
            addComponent(GeometricComponent(this, x, y))
            addComponent(PowerUpTypeComponent(this, powerUpType))
            addComponent(PowerUpRenderableComponent(this))
        }
    }
}