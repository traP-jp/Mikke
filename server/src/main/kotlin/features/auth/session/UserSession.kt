package jp.trap.mikke.features.auth.session

import kotlinx.serialization.Serializable
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
data class UserSession(
    val userId: Uuid,
    val name: String,
    val accessToken: String,
    val refreshToken: String?,
    val expiresAt: Instant,
)
