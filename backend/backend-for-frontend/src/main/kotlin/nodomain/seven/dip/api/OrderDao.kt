package nodomain.seven.dip.api

import nodomain.seven.dip.game.GameDAO
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.nio.file.Files
import java.nio.file.Path

class OrderDao(gameId: String) {
    val orderFilePath: Path = GameDAO.gameDataPath.resolve(gameId).resolve(".countries")
    init {
        if (!Files.exists(orderFilePath.parent))
            throw IllegalArgumentException("Game does not exist")
        if (!Files.exists(orderFilePath))
            Files.createDirectory(orderFilePath)
    }

    fun loadOrders(country: String): OrderWriteUp {
        val saveOrdersPath = orderFilePath.resolve(country).resolve("currentOrders.ser")
        return ObjectInputStream(BufferedInputStream(FileInputStream(saveOrdersPath.toFile()))).use {
            it.readObject() as OrderWriteUp
        }
    }

    fun createIfNotExists(country: String) {
        val ordersPath = orderFilePath.resolve(country)
        if (!Files.exists(ordersPath)) {
            Files.createDirectory(ordersPath)
            Files.createFile(ordersPath.resolve("gameObject.ser"))
            Files.createFile(ordersPath.resolve("tokenLog.ser"))
        }
    }

    fun saveOrders(country: String, orders: OrderWriteUp) {
        val ordersPath = orderFilePath.resolve(country)
        ObjectOutputStream(
            BufferedOutputStream(
                FileOutputStream(
                    ordersPath.resolve("currentOrders.ser").toFile()
                )
            )
        ).use {
            it.writeObject(orders)
        }
    }
}

