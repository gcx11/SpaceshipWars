package me.gcx11.spaceshipwars.spaceship

import me.gcx11.spaceshipwars.bullet.BulletSourceComponent
import me.gcx11.spaceshipwars.components.BehaviourComponent
import me.gcx11.spaceshipwars.components.DamagingComponent
import me.gcx11.spaceshipwars.components.DisposableComponent
import me.gcx11.spaceshipwars.events.CollisionEvent
import me.gcx11.spaceshipwars.events.EntityDeathEvent
import me.gcx11.spaceshipwars.events.collisionEventHandler
import me.gcx11.spaceshipwars.events.entityDeathEvent
import me.gcx11.spaceshipwars.models.Entity
import me.gcx11.spaceshipwars.models.World

class SpaceshipCrashComponent(
    override val parent: Entity
) : BehaviourComponent, DisposableComponent {
    init {
        collisionEventHandler += this::onSpaceshipCrash
    }

    private fun onSpaceshipCrash(event: CollisionEvent) {
        val first = event.first
        val second = event.second

        if (parent in listOf(first.parent, second.parent)) {
            val other = if (parent == first.parent) { second.parent } else { first.parent }

            val healthComponent = parent.getRequiredComponent<HealthComponent>()
            val damagingComponent = other.getOptionalComponent<DamagingComponent>()

            if (damagingComponent != null && healthComponent.isAlive()) {

                // friendly fire
                val bulletSourceComponent = other.getOptionalComponent<BulletSourceComponent>()
                if (bulletSourceComponent?.parent == parent) return

                if (parent == event.first.parent && event.first is ShieldComponent) {
                    // no shield damage
                } else if (parent == event.second.parent && event.second is ShieldComponent) {
                    // no shield damage
                } else {
                    healthComponent.applyDamage(damagingComponent.damage)
                }

                if (!healthComponent.isAlive()) {
                    World.deleteLater(parent)
                    entityDeathEvent(EntityDeathEvent(parent, other))
                }

                // TODO self-damage
            }
        }
    }

    override fun update(delta: Float) {

    }

    override fun dispose() {
        collisionEventHandler -= this::onSpaceshipCrash
    }
}