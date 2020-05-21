package me.gcx11.spaceshipwars.spaceship

import me.gcx11.spaceshipwars.Camera
import me.gcx11.spaceshipwars.components.CanvasContextRenderableComponent
import me.gcx11.spaceshipwars.components.ClientComponent
import me.gcx11.spaceshipwars.components.GeometricComponent
import me.gcx11.spaceshipwars.models.Entity
import me.gcx11.spaceshipwars.models.World
import me.gcx11.spaceshipwars.serverConnection
import org.w3c.dom.CanvasRenderingContext2D
import kotlin.math.*

class SpaceshipRadarComponent(
    override val parent: Entity,
    val arrowCenterDistance: Float = 35f,
    val arrowSizeDistance: Float = 30f,
    val arrowSidesAngle: Float = PI.toFloat() / 10f,
    val minDistance: Float = 400f
): CanvasContextRenderableComponent {
    override var context: CanvasRenderingContext2D? = null

    override fun draw() {
        val ctx = context ?: return
        val clientComponent = parent.getOptionalComponent<ClientComponent>() ?: return
        val geometricComponent = parent.getOptionalComponent<GeometricComponent>() ?: return

        // skip other players
        if (clientComponent.clientId != serverConnection.id) return

        for (entity in World.getAllEntities()) {
            val entityClientComponent = entity.getOptionalComponent<ClientComponent>() ?: continue
            val entityGeometricComponent = entity.getOptionalComponent<GeometricComponent>() ?: continue

            if (entityClientComponent.clientId != clientComponent.clientId) {
                val distance = hypot(geometricComponent.y - entityGeometricComponent.y, geometricComponent.x - entityGeometricComponent.x)
                if (minDistance > distance) continue

                val points = getPoints(geometricComponent, entityGeometricComponent)
                ctx.beginPath()

                val firstPoint = points.firstOrNull()

                if (firstPoint != null) {
                    val (x, y) = Camera.project(firstPoint.first, firstPoint.second)
                    ctx.moveTo(x.toDouble(), y.toDouble())
                }

                points.drop(1).forEach { (a, b) ->
                    val (x, y) = Camera.project(a, b)
                    ctx.lineTo(x.toDouble(), y.toDouble())
                }

                ctx.closePath()

                ctx.strokeStyle = "red"
                ctx.stroke();
            }
        }
    }

    private fun getPoints(self: GeometricComponent, other: GeometricComponent): List<Pair<Float, Float>> {
        val direction = atan2(other.y - self.y, other.x - self.x)

        return listOf(
            Pair(
                self.x + arrowSizeDistance * cos(direction - arrowSidesAngle),
                self.y + arrowSizeDistance * sin(direction - arrowSidesAngle)
            ),
            Pair(
                self.x + arrowCenterDistance * cos(direction),
                self.y + arrowCenterDistance * sin(direction)
            ),
            Pair(
                self.x + arrowSizeDistance * cos(direction + arrowSidesAngle),
                self.y + arrowSizeDistance * sin(direction + arrowSidesAngle)
            )
        )
    }
}