package jp.trap.mikke.di

import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import jp.trap.mikke.core.traq.infrastructure.TraqApiFactory
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
object TraqClientModule {
    private val client =
        HttpClient(Apache) {
            install(ContentNegotiation) {
                json()
            }
        }

    @Single(createdAtStart = true)
    fun provideApiFactory(): TraqApiFactory = TraqApiFactory(client)
}
