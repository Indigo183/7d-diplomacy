package nodomain.seven.dip.game

import nodomain.seven.dip.adjudication.adjudicate
import nodomain.seven.dip.orders.A
import nodomain.seven.dip.orders.Build
import nodomain.seven.dip.orders.T
import nodomain.seven.dip.orders.input
import nodomain.seven.dip.provinces.Romans.BRU
import nodomain.seven.dip.provinces.Romans.CAE
import nodomain.seven.dip.provinces.Romans.CAT
import nodomain.seven.dip.provinces.Romans.POM
import nodomain.seven.dip.utils.Location
import nodomain.seven.dip.utils.c
import nodomain.seven.dip.utils.i
import nodomain.seven.dip.utils.plus
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

    init {
        if (!Files.exists(filePath)) Files.createDirectories(filePath)
        if (!Files.exists(filePath.resolve(Path("testGame", "gameObject.ser")))) {
            storeGame("testGame", newTestGame())
        }
    }

    fun loadGame(name: String): Game {
        val saveGamePath = filePath.resolve(name).resolve("gameObject.ser")
        return ObjectInputStream(BufferedInputStream(FileInputStream(saveGamePath.toFile()))).use {
            it.readObject() as Game
        }
    }

    fun existingGame(name: String) = Files.exists(filePath.resolve(name))

    fun storeGame(name: String, game: Game) {
        val gamePath = filePath.resolve(name)
        Files.createDirectory(gamePath)
        Files.createFile(gamePath.resolve("gameObject.ser"))
        ObjectOutputStream(BufferedOutputStream(FileOutputStream(gamePath.resolve("gameObject.ser").toFile()))).use { it.writeObject(game) }
    }
}

fun newTestGame(): Game {
    val origin = T(0.c, 0)

    val game = Game()

    game.input(listOf(
        origin A CAT M BRU i 2,
        origin A POM M BRU i 1,
    ))
    game.adjudicate()

    game.input(listOf(
        T(i, 0) A CAT M Location(CAT, origin) i 2,
        T(i, 0) A BRU M Location(CAT, origin) i 2,

        T(-1.c, 0) A BRU M POM i 1,
        T(-1.c, 0) A POM M Location(POM, origin) i 1,
    ))
    game.adjudicate()
    game.input(listOf(Build(T(-1+i, 0) A CAT)))
    game.adjudicate()

    game.input(listOf(
        T(-1+i, 0) A CAT M Location(CAE, T(i, 0)) i 3,
        T(-1+i, 0) A POM M Location(POM, T(i, 0)) i 3,

        T(i, 1) A CAT S { T(-1+i, 0) A CAT M Location(CAE, T(i, 0)) },
        T(i, 1) A POM M CAE i 3,
        T(i, 1) A BRU S { T(i, 1) A POM M CAE },
    ))
    game.adjudicate()

    return game
}
