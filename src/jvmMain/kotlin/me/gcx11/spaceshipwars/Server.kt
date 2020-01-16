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
import me.gcx11.spaceshipwars.events.*
import me.gcx11.spaceshipwars.models.Spaceship
import me.gcx11.spaceshipwars.models.World
import me.gcx11.spaceshipwars.models.globalEventQueue
import me.gcx11.spaceshipwars.networking.ClientConnection
import me.gcx11.spaceshipwars.packets.*
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

            for (event in events) {
                processEvent(event)
            }

            globalEventQueue.unfreeze()
            delay(sleepTime)
        }
    }
}

fun processEvent(event: Event) {
    when (event) {
        is PacketEvent -> packetEventHandler(event)
        is ClientDisconnectEvent -> clientDisconnectEventHandler(event)
    }
}

fun registerEventHandlers() {
    packetEventHandler += { event ->
        val packet = event.packet

        if (packet is MoveRequestPacket) {
            val entity = World.entities.find { it.id == packet.entityId }
            if (entity != null && entity is Spaceship) {
                when (packet.direction) {
                    0 -> entity.y -= 10f
                    1 -> entity.y += 10f
                    2 -> entity.x -= 10f
                    3 -> entity.x += 10f
                }

                clients.forEach {
                    it.sendPacket(SpaceshipPositionPacket(
                        entity.id, entity.x, entity.y
                    ))
                }
            }
        }
    }

    packetEventHandler += { event ->
        val packet = event.packet

        if (packet is RespawnRequestPacket) {
            val client = clients.find { it.id == packet.clientId }!!
            for (entity in World.entities) {
                if (entity !is Spaceship) continue

                client.sendPacket(SpaceshipSpawnPacket(
                    0, entity.id, entity.x, entity.y
                ))
            }

            val spaceship = Spaceship(packet.clientId, rnd.nextFloat() * 400.0f, rnd.nextFloat() * 400.0f)
            World.entities.add(spaceship)

            clients.forEach {
                it.sendPacket(SpaceshipSpawnPacket(
                    if (it.id == packet.clientId) packet.clientId else 0, spaceship.id, spaceship.x, spaceship.y
                ))
            }
        }
    }

    clientDisconnectEventHandler += { event ->
        val clientEntity = World.entities.find { it is Spaceship && it.clientId == event.clientId }

        if (clientEntity != null) {
            clients.forEach {
                it.sendPacket(EntityRemovePacket(clientEntity.id))
            }
        }

        World.entities.remove(clientEntity)
    }
}