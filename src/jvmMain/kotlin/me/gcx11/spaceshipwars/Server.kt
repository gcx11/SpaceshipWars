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
import me.gcx11.spaceshipwars.events.PacketEvent
import me.gcx11.spaceshipwars.events.packetEventHandler
import me.gcx11.spaceshipwars.networking.ClientConnection
import me.gcx11.spaceshipwars.packets.*
import java.util.concurrent.CopyOnWriteArrayList

val clients = CopyOnWriteArrayList<ClientConnection>()

fun main() {
    launchGameloop()

    embeddedServer(Netty, port = 8080, host = "127.0.0.1") {
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
                clients.add(client)
                send(Frame.Binary(true, serialize(ClientJoinPacket(client.id))))

                for (frame in incoming) {
                    when (frame) {
                        is Frame.Binary -> {
                            val incomingPacket = deserialize(frame.readBytes())!!
                            PacketQueue.incoming.push(incomingPacket)
                        }
                    }

                    send(Frame.Binary(true, serialize(client.getNextPacket())))
                }
            }

            static("/static") {
                resource("SpaceshipWars.js")
            }
        }
    }.start(wait = true)
}

fun launchGameloop() {
    packetEventHandler += { event ->
        if (event.packet is NoopPacket) {
            // ingore
        }
    }

    GlobalScope.launch {
        while (true) {
            val packets = PacketQueue.incoming.freeze()

            for (packet in packets) {
                processPacket(packet)
            }

            PacketQueue.incoming.unfreeze()
            delay(10L)
        }
    }
}

fun processPacket(packet: Packet) {
    packetEventHandler(PacketEvent(packet))
}