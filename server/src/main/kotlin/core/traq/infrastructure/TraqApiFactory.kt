package jp.trap.mikke.core.traq.infrastructure

import io.ktor.client.HttpClient
import jp.trap.mikke.config.Environment
import jp.trap.mikke.traq.client.infrastructure.ApiClient

class TraqApiFactory(
    val httpClient: HttpClient,
) {
    val baseUrl = Environment.TRAQ_API_BASE_URL

    inline fun <reified T : ApiClient> createApi(
        constructor: (String, HttpClient) -> T,
        token: String,
    ): T =
        constructor(baseUrl, httpClient).apply {
            setAccessToken(token)
        }
}
