package me.gcx11.spaceshipwars

object Camera {
    private var x = 0f
    private var y = 0f

    private var width = 0f
    private var height = 0f

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
}