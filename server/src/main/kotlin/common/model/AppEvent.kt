package jp.trap.mikke.common.model

sealed interface AppEvent {
    val type: String
}
