package nodomain.seven.dip.api

import nodomain.seven.dip.game.GameDAO
import nodomain.seven.dip.utils.FileDAO
import java.nio.file.Files
import java.nio.file.Path

class OrderDao(gameId: String): FileDAO<String, OrderWriteUp>() {
    val orderFilePath: Path = GameDAO.gameDataPath.resolve(gameId).resolve(".countries")
    init {
        if (!Files.exists(orderFilePath.parent))
            throw IllegalArgumentException("Game does not exist")
        if (!Files.exists(orderFilePath))
            Files.createDirectory(orderFilePath)
    }

    override fun getPath(identifier: String): Path = orderFilePath.resolve(identifier).resolve("currentOrders.ser")

    override fun onCreation(identifier: String, creationPath: Path) {
        Files.createFile(orderFilePath.resolve(identifier).resolve("tokenLog.ser"))
    }
}

