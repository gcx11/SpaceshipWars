package me.gcx11.spaceshipwars.bullet

import me.gcx11.spaceshipwars.components.FillStyle
import me.gcx11.spaceshipwars.components.ShapeRenderableComponent
import me.gcx11.spaceshipwars.models.Entity

object BulletFactory {
    fun createBullet(entityId: Long, x: Float, y: Float, speed: Float, direction: Float): Entity {
        return Entity(entityId).apply {
            addComponent(GeometricComponent(this, x, y, direction))
            addComponent(ShapeRenderableComponent(this, FillStyle.OUTLINE))
            addComponent(MovePredictionComponent(this, direction, speed))
        }
    }
}