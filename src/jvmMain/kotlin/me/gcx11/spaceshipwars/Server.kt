package me.gcx11.spaceshipwars

import io.ktor.application.call
import io.ktor.html.respondHtml
import io.ktor.http.content.resource
import io.ktor.http.content.static
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.html.*

fun main() {
    embeddedServer(Netty, port = 8080, host = "127.0.0.1") {
        routing {
            get("/") {
                call.respondHtml {
                    head {
                        title("Spaceship Wars")
                    }
                    body {
                        +message
                        script(src = "/static/SpaceshipWars.js") {}
                    }
                }
            }
            static("/static") {
                resource("SpaceshipWars.js")
            }
        }
    }.start(wait = true)
}