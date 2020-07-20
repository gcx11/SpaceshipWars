package me.gcx11.spaceshipwars.spaceship

import me.gcx11.spaceshipwars.components.BehaviourComponent
import me.gcx11.spaceshipwars.components.CollidableComponent
import me.gcx11.spaceshipwars.components.DefaultCollidableComponent
import me.gcx11.spaceshipwars.geometry.Circle
import me.gcx11.spaceshipwars.geometry.Shape
import me.gcx11.spaceshipwars.models.Entity
import me.gcx11.spaceshipwars.shieldRadius
import kotlin.math.max

class ShieldComponent(
    override val parent: Entity
): BehaviourComponent, DefaultCollidableComponent(parent) {
    val defaultShieldDuration = 10f
    var currentShieldDuration = 0f
        private set

    override fun update(delta: Float) {
        currentShieldDuration = max(currentShieldDuration - delta, 0f)
    }

    fun applyShield() {
        currentShieldDuration = defaultShieldDuration
    }

    val hasShield get() = (currentShieldDuration > 0f)

    override fun getShape(): Shape? {
        val geometricComponent = parent.getRequiredComponent<GeometricComponent>()

        return if (hasShield) Circle(geometricComponent.center, shieldRadius) else null
    }
}