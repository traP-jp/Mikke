package jp.trap.mikke.core.event

import jp.trap.mikke.core.event.model.AppEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance

interface EventBus {
    suspend fun emit(event: AppEvent)

    val events: Flow<AppEvent>
}

suspend inline fun <reified T : AppEvent> EventBus.on(crossinline handler: suspend (event: T) -> Unit) =
    events.filterIsInstance<T>().collect {
        handler(it)
    }
