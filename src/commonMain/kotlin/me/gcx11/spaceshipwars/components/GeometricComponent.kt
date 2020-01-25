package me.gcx11.spaceshipwars.components

import me.gcx11.spaceshipwars.geometry.Shape

interface GeometricComponent : Component {
    var x: Float
    var y: Float

    val shape: Shape
}