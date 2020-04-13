package me.gcx11.spaceshipwars.models

import me.gcx11.spaceshipwars.collections.SwapQueue
import me.gcx11.spaceshipwars.events.*

val globalEventQueue = SwapQueue<Event>()

object World {
    private val entities = mutableListOf<Entity>()
    private val entitiesToAdd = mutableListOf<Entity>()
    private val entitiesToDelete = mutableListOf<Entity>()

    fun getAllEntites(): List<Entity> {
        return entities
    }

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

        entities.addAll(entitiesToAdd)
        entitiesToAdd.clear()
    }

    fun deleteOldEntities() {
        entitiesToDelete.forEach {
            removeEntityEventHandler(RemoveEntityEvent(it))
        }

        entities.removeAll(entitiesToDelete)
        entitiesToDelete.clear()
    }
}