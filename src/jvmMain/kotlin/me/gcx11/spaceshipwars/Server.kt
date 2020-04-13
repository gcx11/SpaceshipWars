package me.gcx11.spaceshipwars

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
import me.gcx11.spaceshipwars.components.BehaviourComponent
import me.gcx11.spaceshipwars.components.ClientComponent
import me.gcx11.spaceshipwars.components.GeometricComponent
import me.gcx11.spaceshipwars.events.*
import me.gcx11.spaceshipwars.models.World
import me.gcx11.spaceshipwars.models.globalEventQueue
import me.gcx11.spaceshipwars.networking.ClientConnection
import me.gcx11.spaceshipwars.packets.*
import me.gcx11.spaceshipwars.spaceship.SpaceshipFactory
import me.gcx11.spaceshipwars.spaceship.SpaceshipFireComponent
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.random.Random

val clients = CopyOnWriteArrayList<ClientConnection>()
val rnd = Random.Default

fun main() {
    launchGameloop()

    embeddedServer(Netty, port = serverPort, host = serverIp) {
        install(WebSockets)

        routing {
            get("/") {
                call.respondHtml {
                    head {
                        title("Spaceship Wars")

                        style {
                            unsafe {
                                raw("""
                                    html, body {
                                        width: 100%;
                                        height: 100%;
                                        margin: 0px;
                                        border: 0;
                                        overflow: hidden; /*  Disable scrollbars */
                                        display: block;  /* No floating content on sides */
                                    }
                                """.trimIndent())
                            }
                        }
                    }
                    body {
                        script(src = "/static/SpaceshipWars.js") {}
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

    GlobalScope.launch {
        while (true) {
            val events = globalEventQueue.freeze()
            World.deleteOldEntities()
            World.addNewEntities()

            for (event in events) {
                processEvent(event)
            }

            // TODO better delta
            val delta = sleepTime / 1000f

            val positions = mutableListOf<EntityPosition>()
            for (entity in World.getAllEntites()) {
                entity.getAllComponents<BehaviourComponent>().forEach { it.update(delta) }

                // TODO use MoveEvent
                val geometricComponent = entity.getOptionalComponent<GeometricComponent>()
                if (geometricComponent != null && geometricComponent is me.gcx11.spaceshipwars.spaceship.GeometricComponent) {
                    positions.add(EntityPosition(entity.externalId, geometricComponent.x, geometricComponent.y, geometricComponent.directionAngle))
                } else if (geometricComponent != null && geometricComponent is me.gcx11.spaceshipwars.bullet.GeometricComponent) {
                    positions.add(EntityPosition(entity.externalId, geometricComponent.x, geometricComponent.y, geometricComponent.directionAngle))
                }
            }

            clients.forEach {
                it.sendPacket(
                    EntityPositionPacket(positions)
                )
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
            val entity = World.getAllEntites().find { it.externalId == packet.entityId }

            val geometricComponent = entity?.getOptionalComponent<GeometricComponent>()
            if (geometricComponent != null && geometricComponent is me.gcx11.spaceshipwars.spaceship.GeometricComponent) {
                geometricComponent.directionAngle = packet.direction
            }
        }
    }

    packetEventHandler += { event ->
        val packet = event.packet

        if (packet is RespawnRequestPacket) {
            val client = clients.find { it.id == packet.clientId }!!
            for (entity in World.getAllEntites()) {
                val geometricComponent = entity.getOptionalComponent<GeometricComponent>() ?: continue

                client.sendPacket(
                    // TODO zero uuid?
                    SpaceshipSpawnPacket(UUID.new(), entity.externalId, geometricComponent.x, geometricComponent.y)
                )
            }

            val x = rnd.nextFloat() * 400.0f
            val y = rnd.nextFloat() * 400.0f
            val spaceship = SpaceshipFactory.create(x, y, packet.clientId)
            World.addLater(spaceship)

            clients.forEach {
                it.sendPacket(
                    SpaceshipSpawnPacket(
                        // TODO zero uuid?
                        if (it.id == packet.clientId) packet.clientId else UUID.new(), spaceship.externalId, x, y
                    )
                )
            }
        }
    }

    packetEventHandler += { event ->
        val packet = event.packet

        if (packet is FirePacket) {
            val entity = World.getAllEntites().find { it.externalId == packet.entityId }

            val spaceshipFireComponent = entity?.getOptionalComponent<SpaceshipFireComponent>()
            spaceshipFireComponent?.requestFire()
        }
    }

    clientDisconnectEventHandler += { event ->
        val clientEntities = World.getAllEntites().filter {
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
        /*
        clients.forEach {
            val entity = event.entity


        }*/

        // TODO
    }

    removeEntityEventHandler += { event ->
        clients.forEach { client ->
            client.sendPacket(EntityRemovePacket(event.entity.externalId))
        }
    }
}