package me.gcx11.spaceshipwars.spaceship

import me.gcx11.spaceshipwars.Logger
import me.gcx11.spaceshipwars.components.BehaviourComponent
import me.gcx11.spaceshipwars.components.getRequiredSibling
import me.gcx11.spaceshipwars.geometry.Vector2
import me.gcx11.spaceshipwars.models.Entity
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

class MovePredictionComponent(
    override val parent: Entity,
    private val initialDirection: Float,
    private val speed: Float = 100f
): BehaviourComponent {
    private val softThreshold = 10.0f
    private val hardThreshold = 50.0f

    private var lastServerX: Float? = null
    private var lastServerY: Float? = null
    private var lastServerDirection: Float? = null

    private var correctionVector: Vector2 = Vector2(0.0f, 0.0f)

    override fun update(delta: Float) {
        val geometricComponent = parent.getRequiredComponent<GeometricComponent>()
        geometricComponent.directionAngle = direction()

        updatePosition(delta)

        if (lastServerX != null && lastServerY != null && lastServerDirection != null) {
            val distance = hypot(abs(geometricComponent.x - lastServerX!!), abs(geometricComponent.y - lastServerY!!))

            Logger.client.info { "Server position (${lastServerX}, ${lastServerY}); Client position: (${geometricComponent.x}, ${geometricComponent.y})" }
            Logger.client.info { "Distance: $distance" }

            if (distance > hardThreshold) {
                Logger.client.info { "Overriding client position" }
                geometricComponent.x = lastServerX!!
                geometricComponent.y = lastServerY!!
            } else if (distance > softThreshold) {
                Logger.client.info { "Adjusting client position" }
                correctionVector = Vector2(lastServerX!! - geometricComponent.x, lastServerY!! - geometricComponent.y)
            }

            lastServerX = null
            lastServerY = null
        }
    }

    fun direction(): Float {
        return lastServerDirection ?: initialDirection
    }

    fun supplyServerData(x: Float, y: Float, direction: Float) {
        lastServerX = x
        lastServerY = y
        lastServerDirection = direction
    }

    private fun updatePosition(delta: Float) {
        getRequiredSibling<me.gcx11.spaceshipwars.components.GeometricComponent>().let {
            it.x += speed * cos(direction()) * delta
            it.y += speed * sin(direction()) * delta

            if (correctionVector.length > 1.0) {
                it.x += correctionVector.x * delta
                it.y += correctionVector.y * delta

               correctionVector = correctionVector.times(1.0f - delta)
            }
        }
    }
}