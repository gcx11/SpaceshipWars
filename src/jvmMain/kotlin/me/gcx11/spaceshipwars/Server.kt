package me.gcx11.spaceshipwars

import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.html.respondHtml
import io.ktor.http.cio.websocket.*
import io.ktor.http.content.resource
import io.ktor.http.content.static
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.websocket.WebSockets
import io.ktor.websocket.webSocket
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.html.*
import me.gcx11.spaceshipwars.components.*
import me.gcx11.spaceshipwars.events.*
import me.gcx11.spaceshipwars.geometry.Circle
import me.gcx11.spaceshipwars.models.World
import me.gcx11.spaceshipwars.models.globalEventQueue
import me.gcx11.spaceshipwars.networking.ClientConnection
import me.gcx11.spaceshipwars.packets.*
import me.gcx11.spaceshipwars.powerup.PowerUpFactory
import me.gcx11.spaceshipwars.powerup.PowerUpType
import me.gcx11.spaceshipwars.powerup.PowerUpTypeComponent
import me.gcx11.spaceshipwars.powerupmonitor.PowerUpMonitorFactory
import me.gcx11.spaceshipwars.spaceship.PlayerScoreComponent
import me.gcx11.spaceshipwars.spaceship.SpaceShipNickNameComponent
import me.gcx11.spaceshipwars.spaceship.SpaceshipFactory
import me.gcx11.spaceshipwars.spaceship.SpaceshipFireComponent
import me.gcx11.spaceshipwars.time.getUnixTimeMillis
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.random.Random

val clients = CopyOnWriteArrayList<ClientConnection>()
val playingClients get() = clients.asSequence().filter { it.clientState == ClientState.PLAYING }

val rnd = Random.Default

fun main() {
    launchGameloop()

    embeddedServer(Netty, port = serverPort, host = serverIp) {
        install(WebSockets)

        // simulate network delay
        /*intercept(ApplicationCallPipeline.Features) {
            delay(100)
        }*/

        routing {
            get("/") {
                call.respondHtml {
                    head {
                        title("Spaceship Wars")
                        meta("viewport", content = "width=device-width, initial-scale=1.0")

                        style {
                            unsafe {
                                raw("""
                                    body {
                                        margin: 0;
                                    }
                                    
                                    canvas {
                                        display: block;  /* prevents scrollbar */
                                        width: 100vw;
                                        height: 100vh;
                                    }
                                    
                                    .container {
                                        position: absolute;
                                        z-index: 1;
                                        width: 100vw;
                                        height: 100vh;
                                        display: flex;
                                        justify-content: center;
                                        align-items: center;
                                        top: 0px;
                                        left: 0px;
                                    }
                                    
                                    .container input, select, textarea, button {
                                        font-family: sans-serif;
                                        margin: .3em;
                                        color: #00CED1;
                                        background: transparent;
                                    }
                                    
                                    .container button, .container input {
                                        border: 1px solid #00CED1;
                                        border-radius: 3px;
                                        padding: .5em 1em;
                                        font-weight: bold;
                                    }
                                    
                                    .container button:hover {
                                        cursor: pointer;
                                        background: #00CED1;
                                        color: #000;
                                    }
                                    
                                    .container input::placeholder {
                                        color: #254c4c;
                                        font-weight: normal;
                                    }
                                    
                                    .container input, .container button {
                                        outline: none;
                                    }
                                """.trimIndent())
                            }
                        }
                    }
                    body {
                        script(src = "/static/SpaceshipWars.js") {}
                        canvas {

                        }
                        form(classes = "container") {
                            input(InputType.text) {
                                id = "nickname"
                                autoFocus = true
                                autoComplete = false
                                spellCheck = false
                                placeholder = "Nickname"
                            }

                            button(type = ButtonType.button) {
                                id = "join-game"
                                text("Play")
                            }
                        }
                    }
                }
            }

            webSocket("/ws") {
                val client = ClientConnection()
                send(Frame.Binary(true, serialize(ClientJoinPacket(client.id))))
                clients.add(client)
                globalEventQueue.push(ClientConnectEvent(client.id))

                for (frame in incoming) {
                    when (frame) {
                        is Frame.Binary -> {
                            val incomingPacket = deserialize(frame.readBytes())!!
                            globalEventQueue.push(PacketEvent(incomingPacket))
                        }
                    }

                    // simulate network delay
                    // delay(100)
                    send(Frame.Binary(true, serialize(client.getNextPacket())))
                }

                // handle disconnect
                clients.remove(client)
                globalEventQueue.push(ClientDisconnectEvent(client.id))
            }

            static("/static") {
                resource("SpaceshipWars.js")
            }
        }
    }.start(wait = true)
}

fun launchGameloop() {
    registerEventHandlers()

    var lastTime = getUnixTimeMillis()
    var lastScoreUpdate = getUnixTimeMillis()

    World.addLater(PowerUpMonitorFactory.create())

    GlobalScope.launch {
        while (true) {
            val delta = (getUnixTimeMillis() - lastTime) / 1000.0f
            lastTime = getUnixTimeMillis()
            val events = globalEventQueue.freeze()
            World.deleteOldEntities()
            World.addNewEntities()

            for (event in events) {
                processEvent(event)
            }

            val positions = mutableListOf<EntityPosition>()
            for (entity in World.entities) {
                entity.getAllComponents<BehaviourComponent>().forEach { it.update(delta) }

                // TODO use MoveEvent
                val geometricComponent = entity.getOptionalComponent<GeometricComponent>()
                val moveComponent = entity.getOptionalComponent<MoveComponent>()
                val speed = moveComponent?.speed ?: 0f
                if (geometricComponent != null && geometricComponent is me.gcx11.spaceshipwars.spaceship.GeometricComponent) {
                    positions.add(EntityPosition(entity.externalId, geometricComponent.x, geometricComponent.y, geometricComponent.directionAngle, speed))
                } else if (geometricComponent != null && geometricComponent is me.gcx11.spaceshipwars.bullet.GeometricComponent) {
                    positions.add(EntityPosition(entity.externalId, geometricComponent.x, geometricComponent.y, geometricComponent.directionAngle, speed))
                }
            }

            val collidables = World.entities
                .flatMap { it.getAllComponents<CollidableComponent>() }
                .onEach { it.clearAllCollided() }

            val collisionEvents = mutableListOf<CollisionEvent>()
            for (i in collidables.indices) {
                for (j in i + 1 until collidables.size) {
                    if (collidables[i].collidesWith(collidables[j])) {
                        collidables[i].addCollided(collidables[j])
                        collidables[j].addCollided(collidables[i])

                        collisionEvents.add(CollisionEvent(collidables[i], collidables[j]))
                    }
                }
            }
            collisionEvents.forEach { collisionEventHandler(it) }

            playingClients.forEach {
                it.sendPacket(
                    EntityPositionPacket(positions)
                )
            }

            // update scoreboard
            val currentTime = getUnixTimeMillis()
            if (currentTime - lastScoreUpdate > 5000) {
                lastScoreUpdate = currentTime
                val playerScoreComponents = World.entities.mapNotNull { it.getOptionalComponent<PlayerScoreComponent>() }
                val scores = playerScoreComponents
                    .sortedByDescending { it.score }
                    .map { PlayerScore(it.parent.externalId, it.score) }
                    .take(5)

                playingClients.forEach {
                    it.sendPacket(
                        PlayerScorePacket(scores)
                    )
                }
            }

            globalEventQueue.unfreeze()
            delay(sleepTime)
        }
    }
}

fun processEvent(event: Event) {
    when (event) {
        is PacketEvent -> packetEventHandler(event)
        is ClientConnectEvent -> clientConnectEventHandler(event)
        is ClientDisconnectEvent -> clientDisconnectEventHandler(event)
    }
}

fun registerEventHandlers() {
    packetEventHandler += { event ->
        val packet = event.packet

        if (packet is MoveRequestPacket) {
            val entity = World.entities.find { it.externalId == packet.entityId }

            val geometricComponent = entity?.getOptionalComponent<GeometricComponent>()
            if (geometricComponent != null && geometricComponent is me.gcx11.spaceshipwars.spaceship.GeometricComponent) {
                geometricComponent.directionAngle = packet.direction
            }
        }
    }

    packetEventHandler += { event ->
        val packet = event.packet

        if (packet is RespawnRequestPacket) {
            handleRespawnRequestPacket(packet)
        }
    }

    packetEventHandler += { event ->
        val packet = event.packet

        if (packet is FirePacket) {
            val entity = World.entities.find { it.externalId == packet.entityId }

            val spaceshipFireComponent = entity?.getOptionalComponent<SpaceshipFireComponent>()
            spaceshipFireComponent?.requestFire()
        }
    }

    packetEventHandler += { event ->
        val packet = event.packet

        if (packet is RespawnRequestPacket) {
            Logger.server.info { "RespawnRequestPacket $packet" }
        }
    }

    clientDisconnectEventHandler += { event ->
        val clientEntities = World.entities.filter {
            it.getOptionalComponent<ClientComponent>()?.clientId == event.clientId
        }

        clientEntities.forEach { World.deleteLater(it) }
    }

    clientConnectEventHandler += { event ->
        Logger.server.info { "Client ${event.clientId} connected!" }
    }

    clientDisconnectEventHandler += { event ->
        Logger.server.info { "Client ${event.clientId} disconnected!" }
    }

    spawnEntityEventHandler += { event ->
        val entity = event.entity

        val geometricComponent = entity.getOptionalComponent<me.gcx11.spaceshipwars.bullet.GeometricComponent>()
        val moveComponent = entity.getOptionalComponent<me.gcx11.spaceshipwars.bullet.MoveComponent>()

        if (geometricComponent != null && moveComponent != null) {
            playingClients.forEach {
                it.sendPacket(
                    BulletSpawnPacket(
                        entity.externalId,
                        geometricComponent.x,
                        geometricComponent.y,
                        moveComponent.speed,
                        geometricComponent.directionAngle
                    )
                )
            }
        }
    }

    spawnEntityEventHandler += { event ->
        val entity = event.entity

        val geometricComponent = entity.getOptionalComponent<me.gcx11.spaceshipwars.powerup.GeometricComponent>()
        val powerUpTypeComponent = entity.getOptionalComponent<PowerUpTypeComponent>()
        val circle = geometricComponent?.shape

        if (geometricComponent != null && powerUpTypeComponent != null && circle != null) {
            playingClients.forEach {
                it.sendPacket(PowerUpSpawnPacket(entity.externalId, powerUpTypeComponent.type.ordinal, circle.center.x, circle.center.y))
            }
        }
    }

    removeEntityEventHandler += { event ->
        val entity = event.entity

        playingClients.forEach { client ->
            client.sendPacket(EntityRemovePacket(entity.externalId))
        }

        val clientComponent = entity.getOptionalComponent<ClientComponent>()
        if (clientComponent != null) {
            val entityClient = clients.find { it.id == clientComponent.clientId }
            if (entityClient != null) {
                entityClient.clientState = ClientState.CONNECTED
            }
        }
    }
}

private fun handleRespawnRequestPacket(packet: RespawnRequestPacket) {
    Logger.server.info { "Player nickname: '${packet.nickName}'" }
    val client = clients.find { it.id == packet.clientId }!!
    if (client.clientState == ClientState.PLAYING) {
        Logger.server.info { "Client ${packet.clientId} is already playing!" }
        return
    }

    client.clientState = ClientState.PLAYING
    for (entity in World.entities) {
        val geometricComponent = entity.getOptionalComponent<GeometricComponent>() ?: continue
        val nickNameComponent = entity.getOptionalComponent<SpaceShipNickNameComponent>() ?: continue

        client.sendPacket(
            // TODO zero uuid?
            SpaceshipSpawnPacket(UUID.new(), entity.externalId, geometricComponent.x, geometricComponent.y, nickNameComponent.nickName)
        )
    }

    // send power-ups
    for (entity in World.entities) {
        val geometricComponent = entity.getOptionalComponent<me.gcx11.spaceshipwars.powerup.GeometricComponent>() ?: continue
        val powerUpTypeComponent = entity.getOptionalComponent<PowerUpTypeComponent>() ?: continue

        val circle = geometricComponent.shape as? Circle ?: continue

        client.sendPacket(
            PowerUpSpawnPacket(entity.externalId, powerUpTypeComponent.type.ordinal, circle.center.x, circle.center.y)
        )
    }

    // send shield info
    for (entity in World.entities) {
        val shieldComponent = entity.getOptionalComponent<me.gcx11.spaceshipwars.spaceship.ShieldComponent>() ?: continue

        if (shieldComponent.hasShield) {
            client.sendPacket(
                ActivateShieldPacket(entity.externalId, shieldComponent.currentShieldDuration)
            )
        }
    }

    val x = rnd.nextFloat() * 400.0f
    val y = rnd.nextFloat() * 400.0f
    val spaceship = SpaceshipFactory.create(x, y, packet.clientId, packet.nickName)
    World.addLater(spaceship)

    playingClients.forEach {
        it.sendPacket(
            SpaceshipSpawnPacket(
                // TODO zero uuid?
                if (it.id == packet.clientId) packet.clientId else UUID.new(), spaceship.externalId, x, y, packet.nickName
            )
        )
    }
}