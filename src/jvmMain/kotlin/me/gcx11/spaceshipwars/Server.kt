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
import kotlinx.html.*
import me.gcx11.spaceshipwars.packets.NumberPacket
import me.gcx11.spaceshipwars.packets.deserialize
import me.gcx11.spaceshipwars.packets.serialize

fun main() {
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
                loop@ for (frame in incoming) {
                    when (frame) {
                        is Frame.Binary -> {
                            val incomingPacket = deserialize(frame.readBytes()) as NumberPacket
                            var number = incomingPacket.number

                            println("Received from client: $number")

                            if (number > 1024) {
                                close(CloseReason(CloseReason.Codes.NORMAL, "Bye"))
                                break@loop
                            }

                            outgoing.send(Frame.Binary(true, serialize(NumberPacket(++number))))
                        }
                    }
                }
            }

            static("/static") {
                resource("SpaceshipWars.js")
            }
        }
    }.start(wait = true)
}