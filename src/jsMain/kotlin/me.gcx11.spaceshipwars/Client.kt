package me.gcx11.spaceshipwars

import io.ktor.client.HttpClient
import io.ktor.client.engine.js.Js
import io.ktor.client.features.websocket.WebSockets
import io.ktor.client.features.websocket.ws
import io.ktor.http.HttpMethod
import io.ktor.http.cio.websocket.*
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.browser.document

@KtorExperimentalAPI
fun main() {
    document.addEventListener("DOMContentLoaded", {
        val client = HttpClient(Js) {
            install(WebSockets)
        }

        GlobalScope.launch {
            client.ws(
                method = HttpMethod.Get,
                host = "127.0.0.1",
                port = 8080, path = "/ws"
            ) {
                outgoing.send(Frame.Text("1"))

                loop@ for (frame in incoming) {
                    when (frame) {
                        is Frame.Text -> {
                            var number = frame.readText().toInt()
                            println("Received from server: $number")

                            if (number > 1024) {
                                close(CloseReason(CloseReason.Codes.NORMAL, "Bye"))
                                break@loop
                            }

                            outgoing.send(Frame.Text("${++number}"))
                        }
                    }
                }
            }
        }
    })
}