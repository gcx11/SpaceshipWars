package me.gcx11.spaceshipwars.spaceship

import me.gcx11.spaceshipwars.bullet.BulletSourceComponent
import me.gcx11.spaceshipwars.components.DisposableComponent
import me.gcx11.spaceshipwars.events.EntityDeathEvent
import me.gcx11.spaceshipwars.events.entityDeathEvent
import me.gcx11.spaceshipwars.models.Entity

class PlayerScoreComponent(
    override val parent: Entity
): DisposableComponent {
    var score: Int = 0
        private set

    init {
        entityDeathEvent += this::onEntityDeath
    }

    override fun dispose() {
        entityDeathEvent -= this::onEntityDeath
    }

    fun onEntityDeath(event: EntityDeathEvent) {
        if (event.killer == parent) {
            score += 1
            println("New score: $score")
            return
        }

        val bulletSource = event.killer.getOptionalComponent<BulletSourceComponent>()?.source
        if (bulletSource == parent) {
            score += 1
            println("New score: $score")
            return
        }
    }
}