package me.gcx11.spaceshipwars.background

import me.gcx11.spaceshipwars.Camera
import me.gcx11.spaceshipwars.components.CanvasContextRenderableComponent
import me.gcx11.spaceshipwars.models.Entity
import org.w3c.dom.CanvasRenderingContext2D
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

class BackgroundRenderableComponent(
    override val parent: Entity
) : CanvasContextRenderableComponent {
    override var context: CanvasRenderingContext2D? = null

    private var dataCache = listOf<StarData>()
    private var cameraOldX = 0f
    private var cameraOldY = 0f

    override fun draw() {
        val ctx = context ?: return
        for (data in getStarsData()) {
            ctx.beginPath()
            val (x, y) = Camera.project(data.x.toFloat(), data.y.toFloat())
            ctx.arc(x.toDouble(), y.toDouble(), data.size, 0.0, 2.0 * PI, false)
            ctx.fillStyle = "white"
            ctx.fill()
            ctx.closePath()
        }
    }

    private fun getStarsData(): List<StarData> {
        if (abs(Camera.x - cameraOldX) > 100 || abs(Camera.y - cameraOldY) > 100) {
            dataCache = computeStarsData()
            cameraOldX = Camera.x
            cameraOldY = Camera.y
        }

        return dataCache
    }

    private fun computeStarsData(): List<StarData> {
        val starsData = mutableListOf<StarData>()

        val normalizedCameraX = (Camera.x.toInt() / 100) * 100
        val normalizedCameraY = (Camera.y.toInt() / 100) * 100

        val startX = normalizedCameraX-200
        val startY = normalizedCameraY-200
        val endX = normalizedCameraX+Camera.width.toInt()+200
        val endY = normalizedCameraY+Camera.height.toInt()+200

        for (x in startX..endX step 100) {
            for (y in startY..endY step 100) {
                getStarPosition(x, y)?.let {
                    starsData.add(it)
                }
            }
        }

        return starsData
    }

    private fun getStarPosition(x: Int, y: Int): StarData? {
        val sectorX = x / 100
        val sectorY = y / 100

        if (sin(5 * sectorX.toDouble()) > 0) return null
        if (cos((sectorX + sectorY).toDouble()) < 0) return null

        if ((2*sectorX + sectorY + 1) % 3 != 0) return null

        return StarData(
            x.toDouble() + ((sectorX * sectorY) % 100).toDouble(),
            y.toDouble() + ((31 * sectorX) % 100).toDouble(),
            ((sectorX + sectorY) % 2 + 1).toDouble()
        )
    }

    data class StarData(val x: Double, val y: Double, val size: Double)
}