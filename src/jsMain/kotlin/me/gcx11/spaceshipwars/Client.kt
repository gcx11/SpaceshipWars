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
import me.gcx11.spaceshipwars.background.BackgroundFactory
import me.gcx11.spaceshipwars.bullet.BulletFactory
import me.gcx11.spaceshipwars.components.*
import me.gcx11.spaceshipwars.events.*
import me.gcx11.spaceshipwars.models.World
import me.gcx11.spaceshipwars.models.globalEventQueue
import me.gcx11.spaceshipwars.networking.ServerConnection
import me.gcx11.spaceshipwars.packets.*
import me.gcx11.spaceshipwars.spaceship.SpaceshipFactory
import org.w3c.dom.*
import kotlin.browser.document
import kotlin.browser.window
import kotlin.math.atan2

val serverConnection = ServerConnection()

fun main() {
    window.onload = {
        val context = setupCanvas()
        setupUI()
        launchGameloop(context)
        launchNetworking()
    }

    window.onkeypress = { event ->
        globalEventQueue.push(KeyPressEvent(event.key))
    }

    window.onmousedown = { _ ->
        globalEventQueue.push(MouseDownEvent())
    }

    window.onmousemove = { event ->
        updateMousePosition(event.x.toFloat(), event.y.toFloat())
    }

    window.oncontextmenu = { event ->
        event.preventDefault()
        false
    }
}

fun updateMousePosition(x: Float, y: Float) {
    MousePosition.x = x
    MousePosition.y = y
}

fun setupCanvas(): CanvasRenderingContext2D {
    val canvas = document.getElementsByTagName("canvas")[0] as HTMLCanvasElement
    val context = canvas.getContext("2d") as CanvasRenderingContext2D
    val width = canvas.clientWidth
    val height = canvas.clientHeight
    canvas.width = width
    canvas.height = height

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
    context.font = "12px Arial"
    context.fillStyle = "white"
    context.textAlign = CanvasTextAlign.LEFT
    context.fillText("Client id: ${serverConnection.id}", 0.0, 20.0)
    context.fillText("${serverConnection.clientState}", 0.0, 40.0)

    if (serverConnection.clientState == ClientState.PLAYING) {
        val player = World.entities.find { it.getOptionalComponent<ClientComponent>()?.clientId == serverConnection.id }
        val geometricComponent = player?.getOptionalComponent<GeometricComponent>()
        if (geometricComponent != null) {
            Camera.centerAt(geometricComponent.x, geometricComponent.y)
        }

        for (entity in World.entities) {
            entity.getAllComponents<RenderableComponent>().forEach {
                if (it is CanvasContextRenderableComponent) {
                    it.context = context
                }

                it.draw()
            }
        }
    }
}

fun setupUI() {
    val nickNameInput = document.getElementById("nickname") as? HTMLInputElement?
    nickNameInput?.apply {
        onkeypress = {
            it.keyCode != 13 // disable ENTER key
        }
    }

    val joinGameInput = document.getElementById("join-game") as? HTMLButtonElement?
    joinGameInput?.apply {
        onclick = {
            onGameJoin()
        }
    }
}

private fun onGameJoin() {
    when (serverConnection.clientState) {
        ClientState.CONNECTED -> {
            val nickNameInput = document.getElementById("nickname") as? HTMLInputElement?
            val nickName = nickNameInput?.value.orEmpty().takeIf { it.isNotBlank() } ?: "Unknown"
            serverConnection.sendPacket(RespawnRequestPacket(serverConnection.id, nickName))

            hideContainerUI()
        }
    }
}

private fun hideContainerUI() {
    val form = document.getElementsByClassName("container")[0] as? HTMLFormElement?
    form?.style?.display = "none"
}

private fun displayContainerUI() {
    val form = document.getElementsByClassName("container")[0] as? HTMLFormElement?
    form?.style?.display = "flex"
}

fun launchGameloop(context: CanvasRenderingContext2D) {
    Camera.setDimensions(context.canvas.width.toFloat(), context.canvas.height.toFloat())
    World.addLater(BackgroundFactory.create())
    registerEventHandlers()

    GlobalScope.launch {
        while (true) {
            window.awaitAnimationFrame()
            //globalEventQueue.push(GameTickEvent())
            val events = globalEventQueue.freeze()
            World.deleteOldEntities()
            World.addNewEntities()

            processEvent(GameTickEvent())
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
        is GameTickEvent -> gameTickEventHandler(event)
        is MouseDownEvent -> mouseDownEventHandler(event)
    }
}

@OptIn(KtorExperimentalAPI::class)
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
            try {
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
            } catch (ex: Exception) {
                Logger.client.error { "Error: ${ex.message}" }
            }
        }
    }
}

fun registerEventHandlers() {
    packetEventHandler += { event ->
        val packet = event.packet
        if (packet is ClientJoinPacket) {
            serverConnection.id = packet.clientId
            serverConnection.clientState = ClientState.CONNECTED
        }
    }

    packetEventHandler += { event ->
        val packet = event.packet
        if (packet is SpaceshipSpawnPacket) {
            val spaceship = SpaceshipFactory.create(packet.entityId, packet.x, packet.y, packet.clientId, packet.nickName)
            World.addLater(spaceship)

            if (packet.clientId == serverConnection.id) {
                serverConnection.clientState = ClientState.PLAYING
            }
        }
    }

    packetEventHandler += { event ->
        val packet = event.packet
        if (packet is EntityPositionPacket) {
            for (position in packet.positions) {
                val entity = World.entities.find { it.externalId == position.entityId }
                val geometricComponent = entity?.getOptionalComponent<GeometricComponent>()
                if (geometricComponent != null && geometricComponent is me.gcx11.spaceshipwars.spaceship.GeometricComponent) {
                    geometricComponent.x = position.x
                    geometricComponent.y = position.y
                    geometricComponent.directionAngle = position.direction
                } else if (geometricComponent != null && geometricComponent is me.gcx11.spaceshipwars.bullet.GeometricComponent) {
                    geometricComponent.x = position.x
                    geometricComponent.y = position.y
                    geometricComponent.directionAngle = position.direction
                }
            }
        }
    }

    packetEventHandler += { event ->
        val packet = event.packet
        if (packet is EntityRemovePacket) {
            val entityToRemove = World.entities.find { it.externalId == packet.entityId }

            if (entityToRemove != null) {
                val clientComponent = entityToRemove.getOptionalComponent<ClientComponent>()

                if (clientComponent != null && clientComponent.clientId == serverConnection.id) {
                    // client is now dead, delete everything
                    World.entities.forEach { World.deleteLater(it) }

                    serverConnection.clientState = ClientState.CONNECTED
                    displayContainerUI()
                } else {
                    World.deleteLater(entityToRemove)
                }
            } else {
                Logger.client.warn { "EntityRemovePacket - entity with id ${packet.entityId} not found!" }
            }
        }
    }

    packetEventHandler += { event ->
        val packet = event.packet
        if (packet is BulletSpawnPacket) {
            // TODO handle direction
            val bullet = BulletFactory.createBullet(packet.entityId, packet.x, packet.y, 0f)
            World.addLater(bullet)
        }
    }

    gameTickEventHandler += { _ ->
        val spaceShip = World.entities.find { it.getOptionalComponent<ClientComponent>()?.clientId == serverConnection.id }

        if (spaceShip != null) {
            val direction = atan2(Camera.height / 2f - MousePosition.y, MousePosition.x - Camera.width / 2f)
            serverConnection.sendPacket(MoveRequestPacket(
                serverConnection.id, spaceShip.externalId, 0f, direction
            ))
        }
    }

    mouseDownEventHandler += { _ ->
        when (serverConnection.clientState) {
            ClientState.PLAYING -> {
                val spaceShip = World.entities.find { it.getOptionalComponent<ClientComponent>()?.clientId == serverConnection.id }

                if (spaceShip != null) {
                    serverConnection.sendPacket(FirePacket(serverConnection.id, spaceShip.externalId))
                }
            }
        }
    }
}