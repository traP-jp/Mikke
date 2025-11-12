package jp.trap.mikke.core.event.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface AppEvent {
    @Serializable
    @SerialName("ConnectionEstablished")
    data object ConnectionEstablished : AppEvent
}
