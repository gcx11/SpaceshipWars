package me.gcx11.spaceshipwars.bullet

import me.gcx11.spaceshipwars.bulletSpeed
import me.gcx11.spaceshipwars.components.DefaultCollidableComponent
import me.gcx11.spaceshipwars.components.DefaultDamagingComponent
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
            addComponent(MoveComponent(this, if (shooterMove.hasBoost) bulletSpeed * 2 else bulletSpeed))
            addComponent(DefaultCollidableComponent(this))
            addComponent(DefaultDamagingComponent(this, 1))
            addComponent(BulletSourceComponent(this, shooter))
        }.apply {
            this.externalId = this.internalId
            this.tag = "Bullet"
        }
    }
}