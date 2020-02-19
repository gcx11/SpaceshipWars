package me.gcx11.spaceshipwars.bullet

import me.gcx11.spaceshipwars.models.Entity
import kotlin.math.cos
import kotlin.math.sin

object BulletFactory {
    fun createBullet(shooter: Entity): Entity {
        val shooterGeo = shooter.getRequiredComponent<me.gcx11.spaceshipwars.spaceship.GeometricComponent>()
        val shooterMove = shooter.getRequiredComponent<me.gcx11.spaceshipwars.spaceship.MoveComponent>()

        return Entity().apply {
            addComponent(
                GeometricComponent(
                    this, shooterGeo.noseX + 2f * cos(shooterGeo.directionAngle),
                    shooterGeo.noseY + 2f * sin(shooterGeo.directionAngle),
                    shooterGeo.directionAngle
                )
            )
            addComponent(
                MoveComponent(
                    this,
                    speed = shooterMove.speed + 2f
                )
            )
        }.apply {
            this.externalId = this.internalId
        }
    }
}