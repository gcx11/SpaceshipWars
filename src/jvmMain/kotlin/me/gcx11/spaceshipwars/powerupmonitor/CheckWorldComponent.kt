package me.gcx11.spaceshipwars.powerupmonitor

import me.gcx11.spaceshipwars.components.BehaviourComponent
import me.gcx11.spaceshipwars.models.Entity
import me.gcx11.spaceshipwars.models.World
import me.gcx11.spaceshipwars.powerup.PowerUpFactory
import me.gcx11.spaceshipwars.powerup.PowerUpType
import me.gcx11.spaceshipwars.powerup.PowerUpTypeComponent
import kotlin.random.Random

class CheckWorldComponent(
    override val parent: Entity
): BehaviourComponent {
    private val powerUpTypes = listOf(PowerUpType.BOOST, PowerUpType.REGEN, PowerUpType.SHIELD)
    private val maxPowerUpCount = 3
    private val range = 2000.0

    override fun update(delta: Float) {
        // TODO use remove entity event
        var powerUpCount = World.entities.count { it.hasComponent<PowerUpTypeComponent>() }

        while (powerUpCount < maxPowerUpCount) {
            World.addLater(
                PowerUpFactory.create(
                    powerUpTypes.random(),
                    Random.nextDouble(-range, range).toFloat(),
                    Random.nextDouble(-range, range).toFloat()
                )
            )

            powerUpCount++
        }
    }
}