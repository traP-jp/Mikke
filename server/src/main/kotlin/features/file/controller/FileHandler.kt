package jp.trap.mikke.features.file.controller

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.sessions.*
import io.ktor.utils.io.jvm.javaio.*
import jp.trap.mikke.features.auth.session.UserSession
import jp.trap.mikke.features.file.domain.model.FileId
import jp.trap.mikke.features.file.service.FileService
import jp.trap.mikke.features.user.domain.model.UserId
import jp.trap.mikke.openapi.models.Error
import jp.trap.mikke.openapi.models.FileInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Single
import kotlin.uuid.Uuid
import jp.trap.mikke.features.file.domain.model.FileInfo as DomainFileInfo

@Single
class FileHandler(
    private val fileService: FileService,
) {
    suspend fun handleUploadFile(call: ApplicationCall) {
        val userIdRaw =
            call.sessions.get<UserSession>()?.userId
                ?: throw IllegalStateException("user not logged in")
        val multipart = call.receiveMultipart()

        val part = multipart.readPart()
        if (part !is PartData.FileItem) {
            part?.dispose()
            call.respond(HttpStatusCode.BadRequest, Error("No file part found in the request"))
            return
        }

        try {
            val originalFilename = part.originalFileName ?: "file"
            val contentType = part.contentType?.toString() ?: ContentType.Application.OctetStream.toString()
            val contentLength = part.headers["Content-Length"]?.toLongOrNull()
            val userId = UserId(userIdRaw)

            val info =
                withContext(Dispatchers.IO) {
                    fileService.uploadFile(
                        originalFilename,
                        contentType,
                        userId,
                        contentLength,
                        part.provider().toInputStream(),
                    )
                }

            call.respond(HttpStatusCode.Created, info.toApiModel())
        } finally {
            part.dispose()
            multipart.forEachPart { it.dispose() }
        }
    }

    suspend fun handleDownloadFile(call: ApplicationCall) {
        withFile(call) { info ->
            call.response.header(
                HttpHeaders.CacheControl,
                CacheControl
                    .MaxAge(
                        maxAgeSeconds = 3600,
                        visibility = CacheControl.Visibility.Public,
                    ).toString(),
            )

            fileService.useFileBody(info.id) { body ->
                body ?: run {
                    call.respond(HttpStatusCode.NotFound, Error("File not found"))
                    return@useFileBody
                }

                call.respondBytesWriter(
                    contentType = ContentType.parse(info.mimeType),
                    status = HttpStatusCode.OK,
                    contentLength = info.size,
                ) {
                    body.use { inputStream ->
                        inputStream.copyTo(this.toOutputStream())
                    }
                }
            }
        }
    }

    suspend fun handleDeleteFile(call: ApplicationCall) {
        val userIdRaw =
            call.sessions.get<UserSession>()?.userId
                ?: throw IllegalStateException("user not logged in")
        val userId = UserId(userIdRaw)

        withFile(call) { info ->
            if (info.uploaderId != userId) {
                call.respond(HttpStatusCode.Forbidden, Error("You do not have permission to delete this file"))
                return@withFile
            }

            fileService.deleteFile(info.id)
            call.respond(HttpStatusCode.NoContent)
        }
    }

    suspend fun handleGetFileMeta(call: ApplicationCall) {
        withFile(call) { info ->
            call.respond(info.toApiModel())
        }
    }

    private suspend fun withFile(
        call: ApplicationCall,
        block: suspend (DomainFileInfo) -> Unit,
    ) {
        val fileIdParam =
            call.parameters["fileId"]
                ?: throw IllegalArgumentException("fileId is required")
        val id = FileId(Uuid.parse(fileIdParam))

        val info =
            fileService.getFileInfo(id)
                ?: run {
                    call.respond(HttpStatusCode.NotFound, Error("File not found"))
                    return
                }
        block(info)
    }

    private fun DomainFileInfo.toApiModel(): FileInfo =
        FileInfo(
            id = this.id.value,
            name = this.filename,
            mimeType = this.mimeType,
            propertySize = this.size,
            uploaderId = this.uploaderId.value,
            createdAt = this.createdAt,
        )
}
