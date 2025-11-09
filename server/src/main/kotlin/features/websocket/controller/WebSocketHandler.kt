package jp.trap.mikke.features.websocket.controller

import io.ktor.server.websocket.*
import io.ktor.websocket.*
import jp.trap.mikke.common.EventBus
import jp.trap.mikke.common.model.AppEvent
import jp.trap.mikke.common.on
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Single
import org.slf4j.LoggerFactory

@Single
class WebSocketHandler(
    private val eventBus: EventBus,
    private val json: Json,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    suspend fun handleSession(session: DefaultWebSocketServerSession) {
        val job =
            session.launch {
                eventBus.on<AppEvent> { event ->
                    try {
                        val eventJson = json.encodeToString(event)
                        session.send(Frame.Text(eventJson))
                    } catch (e: Exception) {
                        logger.info("Failed to send event to client: ${e.message}")
                    }
                }
            }

        session.send(json.encodeToString<AppEvent>(AppEvent.ConnectionEstablished))

        try {
            session.incoming.consumeEach {}
        } catch (_: ClosedReceiveChannelException) {
            logger.info("Session closed by client: ${session.closeReason.await()}")
        } catch (e: Exception) {
            logger.error("Error while handling session: ${session.closeReason.await()}", e)
        } finally {
            job.cancel()
        }
    }
}
