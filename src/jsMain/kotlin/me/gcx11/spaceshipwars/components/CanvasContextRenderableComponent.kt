package me.gcx11.spaceshipwars.components

import org.w3c.dom.CanvasRenderingContext2D

interface CanvasContextRenderableComponent : RenderableComponent {
    var context: CanvasRenderingContext2D?
}