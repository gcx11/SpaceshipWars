package me.gcx11.spaceshipwars.spaceship

import me.gcx11.spaceshipwars.UUID
import me.gcx11.spaceshipwars.components.ClientComponent
import me.gcx11.spaceshipwars.components.DefaultCollidableComponent
import me.gcx11.spaceshipwars.components.DefaultDamagingComponent
import me.gcx11.spaceshipwars.models.Entity

actual object SpaceshipFactory {
    fun create(x: Float, y: Float, clientId: UUID, nickName: String): Entity {
        return Entity().apply {
            addComponent(ClientComponent(this, clientId))
            addComponent(SpaceShipNickNameComponent(this, nickName))
            addComponent(GeometricComponent(this, x, y, 20f, 20f, 20f, 0f))
            addComponent(MoveComponent(this))
            addComponent(SpaceshipFireComponent(this))
            addComponent(DefaultCollidableComponent(this))
            addComponent(SpaceshipCrashComponent(this))
            addComponent(HealthComponent(this, 10))
            addComponent(DefaultDamagingComponent(this, 1))
            addComponent(PlayerScoreComponent(this))
        }.apply {
            this.externalId = this.internalId
            this.tag = "Spaceship"
        }
    }
}