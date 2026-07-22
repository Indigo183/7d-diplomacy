package nodomain.seven.dip.utils

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.jsonwebtoken.JwtParser
import io.jsonwebtoken.Jwts
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.inject.Produces
import jakarta.ws.rs.ApplicationPath
import jakarta.ws.rs.core.Application
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import kotlin.io.path.Path

@ApplicationPath("api")
class RestApp: Application()

var filePath = Path(System.getProperty("user.home"), ".7dip")


fun setupFiles(filePath: Path) {
    Files.createDirectories(filePath.resolve("security"))
    val jwtPath = filePath.resolve(Paths.get("security", "JWT_key.ser"))
    Files.createFile(jwtPath)
    ObjectOutputStream(BufferedOutputStream(FileOutputStream(jwtPath.toFile()))).use {
        it.writeObject(KeyGenerator.getInstance("HmacSha256").generateKey())
    }
}

@ApplicationScoped
class JWTParserProvider {
    init {
        if (!Files.exists(filePath)) setupFiles(filePath)
    }
    @Produces
    val key: SecretKey = ObjectInputStream(BufferedInputStream(FileInputStream(
        filePath.resolve(Paths.get("security", "JWT_key.ser")).toFile()
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
}