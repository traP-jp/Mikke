package jp.trap.mikke.di

import jp.trap.mikke.common.model.AppEvent
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import org.koin.dsl.module

object EventSerializationModule {
    val module =
        module {
            single<SerializersModule> {
                SerializersModule {
                    polymorphic(AppEvent::class) {
                        subclass(AppEvent.ConnectionEstablished::class)
                    }
                }
            }

            single<Json> {
                Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                    serializersModule = get()
                    classDiscriminator = "type"
                    encodeDefaults = true
                }
            }
        }
}
