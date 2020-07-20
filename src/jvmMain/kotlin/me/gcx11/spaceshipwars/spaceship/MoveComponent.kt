package me.gcx11.spaceshipwars.spaceship

import me.gcx11.spaceshipwars.components.DefaultMoveComponent
import me.gcx11.spaceshipwars.components.getRequiredSibling
import me.gcx11.spaceshipwars.models.Entity
import kotlin.math.max

class MoveComponent(
    override val parent: Entity,

    val initialSpeed: Float
): DefaultMoveComponent(parent, initialSpeed) {
    private val defaultBoostDuration = 10f
    private var currentBoostDuration = 0f

    override fun update(delta: Float) {
        currentBoostDuration = max(currentBoostDuration - delta, 0f)
        super.update(delta)
    }

    override val speed: Float
        // TODO better formula
        get() = if (hasBoost) super.speed * 2 else super.speed

    override fun direction(): Float {
        return getRequiredSibling<GeometricComponent>().directionAngle
    }

    // TODO boost component
    fun applyBoost() {
        currentBoostDuration = defaultBoostDuration
    }

    val hasBoost get() = (currentBoostDuration > 0f)
}