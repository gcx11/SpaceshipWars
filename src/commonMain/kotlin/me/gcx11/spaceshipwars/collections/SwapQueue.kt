package me.gcx11.spaceshipwars.collections

import kotlinx.atomicfu.locks.synchronized
import kotlinx.atomicfu.locks.SynchronizedObject

class SwapQueue<T> {
    private val lock = SwapQueueLock()

    private var current = mutableListOf<T>()
    private var waiting = mutableListOf<T>()

    fun push(value: T) {
        synchronized(lock) {
            current.add(value)
        }
    }

    fun freeze(): Iterable<T> {
        var temp: MutableList<T>?

        synchronized(lock) {
            temp = current
            current = waiting
            waiting = temp!!
        }

        return waiting
    }

    fun unfreeze() {
        waiting.clear()
    }
}

private class SwapQueueLock: SynchronizedObject()