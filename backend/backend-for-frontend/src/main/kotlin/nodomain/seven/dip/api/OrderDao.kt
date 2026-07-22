package nodomain.seven.dip.api

import nodomain.seven.dip.game.GameDAO
import nodomain.seven.dip.utils.FileDAO
import java.io.Serializable
import java.nio.file.Files
import java.nio.file.Path
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

fun countryDataDirectory(gameId: String): Path = GameDAO.gameDataPath.resolve(gameId).resolve(".countries")

class OrderDao(gameId: String): FileDAO<String, OrderWriteUp>() {
    val orderFilePath: Path = countryDataDirectory(gameId)
    init {
        if (!Files.exists(orderFilePath.parent))
            throw IllegalArgumentException("Game does not exist")
        if (!Files.exists(orderFilePath))
            Files.createDirectory(orderFilePath)
    }

    override fun getPath(identifier: String): Path = orderFilePath.resolve(identifier).resolve("currentOrders.ser")

    override fun onCreation(identifier: String, creationPath: Path) {
        TokenAccess.createIfNotExists(creationPath.parent)
        TokenAccess.save(creationPath.parent, TokenAccess())
    }
}

class TokenAccess(
    var tokenCreatedLog: MutableList<Long> = mutableListOf(),
    var tokenRecoveredLog: MutableList<Long> = mutableListOf()
): Serializable {
    companion object: FileDAO<Path, TokenAccess>() {
        override fun getPath(identifier: Path): Path = identifier.resolve("tokenLog.ser")

        @OptIn(ExperimentalTime::class)
        fun logCreateToken(gameId: String, country: String) {
            val path = countryDataDirectory(gameId).resolve(country)
            val log = load(path)
            log.tokenCreatedLog+= Clock.System.now().epochSeconds
            save(path, log)
        }

        @OptIn(ExperimentalTime::class)
        fun logRecoverToken(gameId: String, country: String) {
            val path = countryDataDirectory(gameId).resolve(country)
            val log = load(path)
            log.tokenRecoveredLog+= Clock.System.now().epochSeconds
            save(path, log)
        }
    }
}
