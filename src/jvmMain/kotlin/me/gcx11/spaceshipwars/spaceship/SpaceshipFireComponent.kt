package me.gcx11.spaceshipwars.spaceship

import me.gcx11.spaceshipwars.bullet.BulletFactory
import me.gcx11.spaceshipwars.clients
import me.gcx11.spaceshipwars.components.BehaviourComponent
import me.gcx11.spaceshipwars.models.Entity
import me.gcx11.spaceshipwars.models.World
import me.gcx11.spaceshipwars.packets.BulletSpawnPacket
import me.gcx11.spaceshipwars.bullet.GeometricComponent

class SpaceshipFireComponent(override val parent: Entity) : BehaviourComponent {
    var wantsFire = true

    override fun update(delta: Float) {
        if (wantsFire) {
            val bullet = BulletFactory.createBullet(parent)
            World.addLater(bullet)
            wantsFire = false

            // TODO use events
            val geometricComponent = bullet.getRequiredComponent<GeometricComponent>()
            clients.forEach {
                it.sendPacket(BulletSpawnPacket(bullet.externalId, geometricComponent.x, geometricComponent.y))
            }
        }
    }
}