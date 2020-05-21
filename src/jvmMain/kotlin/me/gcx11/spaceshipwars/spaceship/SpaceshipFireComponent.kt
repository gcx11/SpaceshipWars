package me.gcx11.spaceshipwars.spaceship

import me.gcx11.spaceshipwars.bullet.BulletFactory
import me.gcx11.spaceshipwars.clients
import me.gcx11.spaceshipwars.components.BehaviourComponent
import me.gcx11.spaceshipwars.models.Entity
import me.gcx11.spaceshipwars.models.World
import me.gcx11.spaceshipwars.packets.BulletSpawnPacket
import me.gcx11.spaceshipwars.bullet.GeometricComponent

class SpaceshipFireComponent(
    override val parent: Entity,
    private val fireDelay: Float = 0.5f
) : BehaviourComponent {
    private var wantsFire = true
    private var fireTimer = 0f

    override fun update(delta: Float) {
        if (fireTimer > 0f) fireTimer -= delta

        if (wantsFire && fireTimer <= 0f) {
            wantsFire = false
            fireTimer = fireDelay

            val bullet = BulletFactory.createBullet(parent)
            World.addLater(bullet)
        }
    }

    fun requestFire() {
        wantsFire = true
    }
}