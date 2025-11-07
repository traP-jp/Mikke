package jp.trap.mikke.di

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.v1.jdbc.Database
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import javax.sql.DataSource

@Module
object DatabaseModule {
    @Single(createdAtStart = true)
    fun provideDataSource(): DataSource {
        println("🚀 Initializing Database Connection...")

        val config = HikariConfig()
        config.jdbcUrl = System.getenv("DB_URL") ?: "jdbc:mariadb://localhost:3306/mikke"
        config.username = System.getenv("DB_USER") ?: "mikke"
        config.password = System.getenv("DB_PASS") ?: "mikke_dev"
        config.driverClassName = "org.mariadb.jdbc.Driver"
        config.maximumPoolSize = 10
        config.validate()

        val dataSource = HikariDataSource(config)

        try {
            Flyway
                .configure()
                .dataSource(dataSource)
                .load()
                .migrate()
        } catch (e: Exception) {
            throw RuntimeException("Failed to migrate database", e)
        }

        Database.connect(dataSource)

        return dataSource
    }
}
