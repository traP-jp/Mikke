package jp.trap.mikke.features.file.infrastructure

import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.DeleteObjectRequest
import aws.sdk.kotlin.services.s3.model.GetObjectRequest
import aws.sdk.kotlin.services.s3.model.HeadObjectRequest
import aws.sdk.kotlin.services.s3.model.PutObjectRequest
import aws.smithy.kotlin.runtime.content.asByteStream
import aws.smithy.kotlin.runtime.content.toInputStream
import aws.smithy.kotlin.runtime.net.url.Url
import jp.trap.mikke.config.Environment
import jp.trap.mikke.features.file.domain.model.FileId
import jp.trap.mikke.features.file.domain.model.FileInfo
import jp.trap.mikke.features.file.service.FileHeader
import jp.trap.mikke.features.file.service.FileStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Single
import java.io.InputStream
import kotlin.time.Clock

@Single(binds = [FileStorage::class])
class S3FileStorage :
    FileStorage,
    AutoCloseable {
    private val bucketName: String = Environment.S3_BUCKET_NAME
    private val client: S3Client

    init {
        require(System.getenv("AWS_ACCESS_KEY_ID") != null) {
            "AWS_ACCESS_KEY_ID is not set"
        }
        require(System.getenv("AWS_SECRET_ACCESS_KEY") != null) {
            "AWS_SECRET_ACCESS_KEY is not set"
        }

        client =
            S3Client {
                endpointUrl = Url.parse(Environment.S3_ENDPOINT_URL)
                region = "us-east-1"
                forcePathStyle = true
            }
    }

    override suspend fun writeFile(
        header: FileHeader,
        data: InputStream,
    ): FileInfo {
        val byteStream = withContext(Dispatchers.IO) { data.asByteStream(header.length) }
        val putObjectRequest =
            PutObjectRequest {
                bucket = bucketName
                key = header.id.toString()
                body = byteStream
                contentType = header.mimeType
            }

        client.putObject(putObjectRequest)

        val headObject =
            HeadObjectRequest {
                bucket = bucketName
                key = header.id.toString()
            }
        val response = client.headObject(headObject)

        return FileInfo(
            id = header.id,
            filename = header.name,
            mimeType = header.mimeType,
            size = response.contentLength ?: 0,
            uploaderId = header.uploaderId,
            createdAt = Clock.System.now(),
        )
    }

    override suspend fun <R> useFile(
        fileId: FileId,
        block: suspend (stream: InputStream?) -> R,
    ): R {
        val getObjectRequest =
            GetObjectRequest {
                bucket = bucketName
                key = fileId.toString()
            }

        return client.getObject(getObjectRequest) { response ->
            val body = response.body ?: return@getObject block(null)
            withContext(Dispatchers.IO) {
                body.toInputStream().use { inputStream ->
                    block(inputStream)
                }
            }
        }
    }

    override suspend fun deleteFile(fileId: FileId) {
        val deleteObjectRequest =
            DeleteObjectRequest {
                bucket = bucketName
                key = fileId.toString()
            }

        client.deleteObject(deleteObjectRequest)
    }

    override fun close() {
        client.close()
    }
}
