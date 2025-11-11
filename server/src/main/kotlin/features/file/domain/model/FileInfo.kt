package jp.trap.mikke.features.file.domain.model

import jp.trap.mikke.features.user.domain.model.UserId
import kotlin.time.Instant

data class FileInfo(
    val id: FileId,
    val filename: String,
    val mimeType: String,
    val size: Long,
    val uploaderId: UserId,
    val createdAt: Instant,
) {
    init {
        require(filename.isNotBlank()) { "Filename must not be blank." }
        require(mimeType.isNotBlank()) { "MimeType must not be blank." }
        require(size >= 0) { "Size must be non-negative." }
    }
}
