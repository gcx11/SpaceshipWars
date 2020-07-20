package me.gcx11.spaceshipwars.models

import me.gcx11.spaceshipwars.collections.SwapQueue
import me.gcx11.spaceshipwars.components.DisposableComponent
import me.gcx11.spaceshipwars.events.*

val globalEventQueue = SwapQueue<Event>()

object World {
    private val entityCollection = mutableListOf<Entity>()
    private val entitiesToAdd = mutableSetOf<Entity>()
    private val entitiesToDelete = mutableSetOf<Entity>()

    val entities: List<Entity> get() = entityCollection

    fun addLater(entity: Entity) {
        entitiesToAdd.add(entity)
    }

    fun deleteLater(entity: Entity) {
        entitiesToDelete.add(entity)
    }

    fun addNewEntities() {
        entitiesToAdd.forEach {
            spawnEntityEventHandler(SpawnEntityEvent(it))
        }

        entityCollection.addAll(entitiesToAdd)
        entitiesToAdd.clear()
    }

    fun deleteOldEntities() {
        entitiesToDelete.flatMap {
            it.getAllComponents<DisposableComponent>()
        }.forEach { it.dispose() }

        entitiesToDelete.forEach {
            removeEntityEventHandler(RemoveEntityEvent(it))
        }

        entityCollection.removeAll(entitiesToDelete)
        entitiesToDelete.clear()
    }
}