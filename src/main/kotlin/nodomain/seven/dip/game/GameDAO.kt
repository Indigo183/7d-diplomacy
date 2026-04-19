package nodomain.seven.dip.game

import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.nio.file.Files
import kotlin.io.path.Path



object GameDAO {
    val filePath = Path(System.getProperty("user.home"), ".7dip")
    lateinit var currentGameName: String
    lateinit var currentGame: Game

    init {
        if (!Files.exists(filePath)) Files.createDirectories(filePath)
        if (!Files.exists(filePath.resolve("currentGame.txt"))) Files.createFile(filePath.resolve("currentGame.txt"))
        val testGamePath = filePath.resolve(Path("testGame", "gameObject.ser"))
        if (!Files.exists(testGamePath)) {
            Files.createDirectories(filePath.resolve("testGame"))
            Files.createFile(testGamePath)
            ObjectOutputStream(BufferedOutputStream(FileOutputStream(testGamePath.toFile()))).use { it.writeObject(newTestGame()) }
        }
        loadCurrentGameName()
        loadCurrentGame()
    }

    fun loadCurrentGameName() {
        currentGameName = Files.newBufferedReader(filePath.resolve("currentGame.txt")).use { it.readLine() ?: "testGame" }
    }
    fun loadCurrentGame() {
        val saveGamePath = filePath.resolve(currentGameName).resolve("gameObject.ser")
        currentGame = ObjectInputStream(BufferedInputStream(FileInputStream(saveGamePath.toFile()))).use {
            it.readObject() as Game
        }
    }
}