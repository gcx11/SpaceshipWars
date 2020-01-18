package me.gcx11.spaceshipwars.collections

inline fun <T, C: MutableCollection<T>> MutableList<T>.retrieveAllTo(collection: C, predicate: (T) -> Boolean): C {
    val iterator = this.listIterator()

    for (item in iterator) {
        if (predicate(item)) {
            collection.add(item)
            iterator.remove()
        }
    }

    return collection
}

inline fun <T> MutableList<T>.retrieveAll(predicate: (T) -> Boolean): List<T> {
    return mutableListOf<T>().also { retrieveAllTo(it, predicate) }
}