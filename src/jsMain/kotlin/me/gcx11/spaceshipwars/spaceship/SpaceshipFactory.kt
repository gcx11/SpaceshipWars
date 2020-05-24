package me.gcx11.spaceshipwars.spaceship

import me.gcx11.spaceshipwars.UUID
import me.gcx11.spaceshipwars.components.ClientComponent
import me.gcx11.spaceshipwars.models.Entity
import kotlin.math.PI

actual object SpaceshipFactory {
    fun create(entityId: Long, x: Float, y: Float, clientId: UUID, nickName: String): Entity {
        return Entity(entityId).apply {
            addComponent(GeometricComponent(this, x, y, 20f, 20f, 20f, PI.toFloat() / 2f))
            addComponent(SpaceshipRenderableComponent(this))
            addComponent(SpaceshipRadarComponent(this))
            addComponent(ClientComponent(this, clientId))
            addComponent(SpaceshipNickNameComponent(this, nickName))
            addComponent(MovePredictionComponent(this, PI.toFloat() / 2f))
        }
    }
}