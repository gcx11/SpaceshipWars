package me.gcx11.spaceshipwars.spaceship

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
        val firstEntity = event.firstEntity
        val secondEntity = event.secondEntity

        if (parent in listOf(firstEntity, secondEntity)) {
            val other = if (parent == firstEntity) secondEntity else firstEntity

            val healthComponent = parent.getRequiredComponent<HealthComponent>()
            val damagingComponent = other.getOptionalComponent<DamagingComponent>()

            if (damagingComponent != null && healthComponent.isAlive()) {
                healthComponent.applyDamage(damagingComponent.damage)
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