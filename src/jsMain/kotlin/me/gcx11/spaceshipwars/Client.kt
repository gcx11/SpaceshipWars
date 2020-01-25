package me.gcx11.spaceshipwars

import io.ktor.client.HttpClient
import io.ktor.client.engine.js.Js
import io.ktor.client.features.websocket.WebSockets
import io.ktor.client.features.websocket.ws
import io.ktor.http.HttpMethod
import io.ktor.http.cio.websocket.*
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.awaitAnimationFrame
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.gcx11.spaceshipwars.components.ClientComponent
import me.gcx11.spaceshipwars.components.GeometricComponent
import me.gcx11.spaceshipwars.components.RenderableComponent
import me.gcx11.spaceshipwars.events.*
import me.gcx11.spaceshipwars.models.World
import me.gcx11.spaceshipwars.models.globalEventQueue
import me.gcx11.spaceshipwars.networking.ServerConnection
import me.gcx11.spaceshipwars.packets.*
import me.gcx11.spaceshipwars.spaceship.ShapeRenderableComponent
import me.gcx11.spaceshipwars.spaceship.SpaceshipFactory
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import kotlin.browser.document
import kotlin.browser.window

val serverConnection = ServerConnection()

fun main() {
    window.onload = {
        val context = createCanvas()
        launchGameloop(context)
        launchNetworking()
    }

    window.onkeypress = { event ->
        globalEventQueue.push(KeyPressEvent(event.key))
    }
}

fun createCanvas(): CanvasRenderingContext2D {
    val canvas = document.createElement("canvas") as HTMLCanvasElement
    val context = canvas.getContext("2d") as CanvasRenderingContext2D
    canvas.width = window.innerWidth
    canvas.height = window.innerHeight
    document.body!!.appendChild(canvas)

    return context
}

fun clearCanvas(context: CanvasRenderingContext2D) {
    context.fillStyle = "#000"

    context.fillRect(
        0.0,
        0.0,
        context.canvas.width.toDouble(),
        context.canvas.height.toDouble()
    )
}

fun draw(context: CanvasRenderingContext2D) {
    context.font = "30px Arial"
    context.fillStyle = "white"
    context.fillText("Client id: ${serverConnection.id}", 100.0, 100.0)

    for (entity in World.entities) {
        val renderableComponent = entity.getOptionalComponent<RenderableComponent>()
        if (renderableComponent is ShapeRenderableComponent) {
            renderableComponent.context = context
        }
        renderableComponent?.draw()
    }
}

fun launchGameloop(context: CanvasRenderingContext2D) {
    registerEventHandlers()

    GlobalScope.launch {
        while (true) {
            window.awaitAnimationFrame()
            val events = globalEventQueue.freeze()

            for (event in events) {
                processEvent(event)
            }

            globalEventQueue.unfreeze()

            clearCanvas(context)
            draw(context)
            delay(sleepTime)
        }
    }
}

fun processEvent(event: Event) {
    when (event) {
        is PacketEvent -> packetEventHandler(event)
        is KeyPressEvent -> keyPressEventHandler(event)
    }
}

@UseExperimental(KtorExperimentalAPI::class)
fun launchNetworking() {
    GlobalScope.launch {
        val client = HttpClient(Js) {
            install(WebSockets)
        }

        client.ws(
            method = HttpMethod.Get,
            host = serverIp,
            port = serverPort, path = "/ws"
        ) {
            for (frame in incoming) {
                when (frame) {
                    is Frame.Binary -> {
                        val incomingPacket = deserialize(frame.readBytes())!!
                        globalEventQueue.push(PacketEvent(incomingPacket))
                    }
                }

                val packet = serverConnection.getNextPacket()
                send(Frame.Binary(true, serialize(packet)))
            }
        }
    }
}

fun registerEventHandlers() {
    packetEventHandler += { event ->
        val packet = event.packet
        if (packet is ClientJoinPacket) {
            serverConnection.id = packet.clientId
            serverConnection.sendPacket(RespawnRequestPacket(packet.clientId))
        }
    }

    packetEventHandler += { event ->
        val packet = event.packet
        if (packet is SpaceshipSpawnPacket) {
            val spaceship = SpaceshipFactory.create(packet.entityId, packet.x, packet.y, packet.clientId)
            World.entities.add(spaceship)
        }
    }

    packetEventHandler += { event ->
        val packet = event.packet
        if (packet is SpaceshipPositionPacket) {
            val entity = World.entities.find { it.id == packet.entityId }
            val geometricComponent = entity?.getOptionalComponent<GeometricComponent>()
            if (geometricComponent != null) {
                geometricComponent.x = packet.x
                geometricComponent.y = packet.y
            }
        }
    }

    packetEventHandler += { event ->
        val packet = event.packet
        if (packet is EntityRemovePacket) {
            World.entities.removeAll { it.id == packet.entityId }
        }
    }

    keyPressEventHandler += { event ->
        val spaceShip = World.entities.find { it.getOptionalComponent<ClientComponent>()?.clientId == serverConnection.id }

        if (spaceShip != null) {
            when (event.key) {
                "w" -> serverConnection.sendPacket(MoveRequestPacket(serverConnection.id, spaceShip.id, 0))
                "s" -> serverConnection.sendPacket(MoveRequestPacket(serverConnection.id, spaceShip.id, 1))
                "a" -> serverConnection.sendPacket(MoveRequestPacket(serverConnection.id, spaceShip.id, 2))
                "d" -> serverConnection.sendPacket(MoveRequestPacket(serverConnection.id, spaceShip.id, 3))
            }
        }
    }
}