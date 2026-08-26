package nodomain.seven.dip.utils

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.jsonwebtoken.JwtParser
import io.jsonwebtoken.Jwts
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.inject.Produces
import jakarta.inject.Inject
import jakarta.ws.rs.ApplicationPath
import jakarta.ws.rs.core.Application
import nodomain.seven.dip.file.FilePathService
import java.io.BufferedInputStream
import java.io.FileInputStream
import java.io.ObjectInputStream
import java.nio.file.Paths
import javax.crypto.SecretKey

@ApplicationPath("api")
class RestApp: Application()

@ApplicationScoped
class JWTParserProvider @Inject constructor(filePathService: FilePathService) {
    @Produces
    val key: SecretKey = ObjectInputStream(BufferedInputStream(FileInputStream(
        filePathService.filePath.resolve(Paths.get("security", "JWT_key.ser")).toFile()
    ))).use {
        it.readObject() as SecretKey
    }

    @Produces
    val jwtParser: JwtParser = Jwts.parser().verifyWith(key).build()!!
}

@ApplicationScoped
class JacksonConfig {
    @Produces
    fun objectMapper(): ObjectMapper =
        ObjectMapper()
            .registerModule(
                KotlinModule.Builder()
                    .build()
            )
            .setPropertyNamingStrategy(
                PropertyNamingStrategies.SNAKE_CASE
            )
}