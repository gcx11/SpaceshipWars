package me.gcx11.spaceshipwars.spaceship

import me.gcx11.spaceshipwars.models.Entity
import kotlin.math.max

class HealthComponent(
    override val parent: Entity,
    private val initialHealth: Int
): me.gcx11.spaceshipwars.components.HealthComponent {
    private var health = initialHealth

    override fun getHealth(): Int {
        return health
    }

    override fun applyDamage(damage: Int) {
        health = max(health - damage, 0)
    }

    override fun isAlive(): Boolean {
        return health > 0
    }

    override fun heal(amount: Int) {
        health = max(health + amount, initialHealth)
    }
}