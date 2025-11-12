package jp.trap.mikke.core.event.infrastructure

import jp.trap.mikke.core.event.EventBus
import jp.trap.mikke.core.event.model.AppEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.koin.core.annotation.Single

@Single(binds = [EventBus::class])
class EventBusImpl : EventBus {
    private val _events =
        MutableSharedFlow<AppEvent>(
            replay = 0,
            extraBufferCapacity = 64,
        )

    override val events = _events.asSharedFlow()

    override suspend fun emit(event: AppEvent) {
        _events.emit(event)
    }
}
