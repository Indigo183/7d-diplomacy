package nodomain.seven.dip.file

import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.inject.Produces
import jakarta.inject.Inject
import nodomain.seven.dip.game.GameDAO
import nodomain.seven.dip.game.newTestGame
import java.io.BufferedOutputStream
import java.io.FileOutputStream
import java.io.ObjectOutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import javax.crypto.KeyGenerator
import kotlin.io.path.Path

@ApplicationScoped
class FilePathService @Inject constructor(val filePath: Path) {
    init {
        if (!Files.exists(filePath)) setupFiles(filePath)
        if (!Files.exists(gameDataPath)) Files.createDirectory(gameDataPath)
        if (!Files.exists(gameDataPath.resolve(Path("testGame", "gameObject.ser")))) {
            GameDAO(this).createAndSave("testGame", newTestGame())
        }
    }

    val gameDataPath: Path get() = filePath.resolve("hosted-games")

    fun countryDataDirectory(gameId: String): Path =
        gameDataPath.resolve(gameId).resolve(".countries")
}

@ApplicationScoped
open class BaseFilePathProvider {
    @Produces
    open fun filePath() = Path(System.getProperty("user.home"), ".7dip")
}

fun setupFiles(filePath: Path) {
    Files.createDirectories(filePath.resolve("security"))
    val jwtPath = filePath.resolve(Paths.get("security", "JWT_key.ser"))
    Files.createFile(jwtPath)
    ObjectOutputStream(BufferedOutputStream(FileOutputStream(jwtPath.toFile()))).use {
        it.writeObject(KeyGenerator.getInstance("HmacSha256").generateKey())
    }
}
