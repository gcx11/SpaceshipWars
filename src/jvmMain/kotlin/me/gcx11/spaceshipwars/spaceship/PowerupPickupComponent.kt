package me.gcx11.spaceshipwars.spaceship

import me.gcx11.spaceshipwars.components.BehaviourComponent
import me.gcx11.spaceshipwars.components.DisposableComponent
import me.gcx11.spaceshipwars.events.CollisionEvent
import me.gcx11.spaceshipwars.events.collisionEventHandler
import me.gcx11.spaceshipwars.models.Entity
import me.gcx11.spaceshipwars.models.World
import me.gcx11.spaceshipwars.packets.ActivateShieldPacket
import me.gcx11.spaceshipwars.playingClients
import me.gcx11.spaceshipwars.powerup.PowerUpType
import me.gcx11.spaceshipwars.powerup.PowerUpTypeComponent

class PowerupPickupComponent(
    override val parent: Entity
) : BehaviourComponent, DisposableComponent {
    init {
        collisionEventHandler += this::onPowerUpCollision
    }

    private fun onPowerUpCollision(event: CollisionEvent) {
        val firstEntity = event.first.parent
        val secondEntity = event.second.parent

        if (parent in listOf(firstEntity, secondEntity)) {
            val other = if (parent == firstEntity) secondEntity else firstEntity
            val powerUpTypeComponent = other.getOptionalComponent<PowerUpTypeComponent>() ?: return

            when (powerUpTypeComponent.type) {
                PowerUpType.REGEN -> {
                    val healthComponent = parent.getRequiredComponent<HealthComponent>()
                    if (healthComponent.isAlive()) {
                        healthComponent.heal(10)
                        World.deleteLater(other)
                    }
                }

                PowerUpType.BOOST -> {
                    val moveComponent = parent.getRequiredComponent<MoveComponent>()
                    moveComponent.applyBoost()
                    World.deleteLater(other)
                }

                PowerUpType.SHIELD -> {
                    val shieldComponent = parent.getRequiredComponent<ShieldComponent>()
                    shieldComponent.applyShield()

                    playingClients.forEach {
                        it.sendPacket(
                            ActivateShieldPacket(parent.externalId, shieldComponent.currentShieldDuration)
                        )
                    }
                    World.deleteLater(other)
                }

                else -> {

                }
            }
        }
    }

    override fun update(delta: Float) {

    }

    override fun dispose() {
        collisionEventHandler -= this::onPowerUpCollision
    }
}