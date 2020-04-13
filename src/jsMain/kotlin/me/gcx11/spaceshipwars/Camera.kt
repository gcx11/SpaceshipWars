package me.gcx11.spaceshipwars

object Camera {
    var x = 0f
        private set
    var y = 0f
        private set

    var width = 0f
        private set

    var height = 0f
        private set

    fun centerAt(x: Float, y: Float) {
        this.x = x - width / 2f
        this.y = y - height / 2f
    }

    fun setDimensions(width: Float, length: Float) {
        this.width = width
        this.height = length
    }

    fun project(x: Float, y: Float): Pair<Float, Float> {
        return Pair(x - this.x, height - (y - this.y))
    }

    fun unproject(x: Float, y: Float): Pair<Float, Float> {
        return Pair(x + this.x, height - (y - this.y))
    }
}