package jp.trap.mikke.features.websocket.controller

import io.ktor.websocket.*
import jp.trap.mikke.common.EventBus
import jp.trap.mikke.common.model.AppEvent
import jp.trap.mikke.common.on
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Single

@Single
class WebSocketHandler(
    private val eventBus: EventBus,
    private val json: Json,
) {
    fun handleSession(session: WebSocketSession) {
        session.launch {
            eventBus.on<AppEvent> { event ->
                val eventJson = json.encodeToString(event)
                session.send(Frame.Text(eventJson))
            }
        }
    }
}
