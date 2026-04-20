package nodomain.seven.dip.utils

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

val filePath = Path(System.getProperty("user.home"), ".7dip")


fun setupJWT(filePath: Path) {
    Files.createDirectories(filePath.resolve("security"))
    val jwtPath = filePath.resolve(Paths.get("security", "JWT_key.ser"))
    Files.createFile(jwtPath)
    ObjectOutputStream(BufferedOutputStream(FileOutputStream(jwtPath.toFile()))).use {
        it.writeObject(KeyGenerator.getInstance("HmacSha256").generateKey())
    }
}

@ApplicationScoped
class JWTKeyProvider {
    init {
        if (!Files.exists(filePath)) setupJWT(filePath)
    }
    @Produces
    val key: SecretKey = ObjectInputStream(BufferedInputStream(FileInputStream(
        filePath.resolve(Paths.get("security", "JWT_key.ser")).toFile()
    ))).use {
        it.readObject() as SecretKey
    }
}