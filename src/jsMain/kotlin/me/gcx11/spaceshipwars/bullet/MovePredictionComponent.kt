package me.gcx11.spaceshipwars.bullet

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
    private val speed: Float
): BehaviourComponent {
    private val softThreshold = 10.0f
    private val hardThreshold = 50.0f

    private var lastServerX: Float? = null
    private var lastServerY: Float? = null
    private var lastServerDirection: Float? = null

    private var correctionVector: Vector2 = Vector2(0.0f, 0.0f)

    override fun update(delta: Float) {
        val geometricComponent = parent.getRequiredComponent<GeometricComponent>()

        updatePosition(delta)

        if (lastServerX != null && lastServerY != null && lastServerDirection != null) {
            val distance = hypot(abs(geometricComponent.x - lastServerX!!), abs(geometricComponent.y - lastServerY!!))

            Logger.client.info { "Server position (${lastServerX}, ${lastServerY}); Client position: (${geometricComponent.x}, ${geometricComponent.y})" }
            Logger.client.info { "Distance: $distance" }

            if (distance > hardThreshold) {
                Logger.client.info { "Overriding bullet position" }
                geometricComponent.x = lastServerX!!
                geometricComponent.y = lastServerY!!
            } else if (distance > softThreshold) {
                Logger.client.info { "Adjusting bullet position" }
                correctionVector = Vector2(lastServerX!! - geometricComponent.x, lastServerY!! - geometricComponent.y)
            }

            lastServerX = null
            lastServerY = null
        }
    }

    fun supplyServerData(x: Float, y: Float) {
        lastServerX = x
        lastServerY = y
    }

    private fun updatePosition(delta: Float) {
        getRequiredSibling<me.gcx11.spaceshipwars.components.GeometricComponent>().let {
            it.x += speed * cos(initialDirection) * delta
            it.y += speed * sin(initialDirection) * delta

            if (correctionVector.length > 1.0) {
                it.x += correctionVector.x * delta
                it.y += correctionVector.y * delta

                correctionVector = correctionVector.times(1.0f - delta)
            }
        }
    }
}