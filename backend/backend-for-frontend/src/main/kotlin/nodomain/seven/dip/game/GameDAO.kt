package nodomain.seven.dip.game

import jakarta.enterprise.context.Dependent
import jakarta.inject.Inject
import nodomain.seven.dip.adjudication.adjudicate
import nodomain.seven.dip.file.FileDAO
import nodomain.seven.dip.file.FilePathService
import nodomain.seven.dip.orders.A
import nodomain.seven.dip.orders.Build
import nodomain.seven.dip.orders.T
import nodomain.seven.dip.orders.input
import nodomain.seven.dip.provinces.Romans.*
import nodomain.seven.dip.utils.Location
import nodomain.seven.dip.utils.c
import nodomain.seven.dip.utils.i
import nodomain.seven.dip.utils.plus
import java.nio.file.Files
import java.nio.file.Path
import java.util.stream.Collectors.toList

@Dependent
class GameDAO @Inject constructor(filePathService: FilePathService): FileDAO<String, Game>() {
    val gameDataPath: Path = filePathService.gameDataPath
    private val signUpDAO = SignUpDAO()

    override fun getPath(identifier: String): Path =
        gameDataPath.resolve(identifier).resolve("gameObject.ser")

    fun loadSignUps(name: String): SignUps = signUpDAO.load(name)

    fun allGames(): List<String> = Files.walk(gameDataPath, 1)
        .filter(Files::isDirectory)
        .map { it.getName(it.nameCount - 1).toString() }
        .collect(toList()).also { it.removeFirst() }

    fun existingGame(name: String) = Files.exists(gameDataPath.resolve(name))

    fun createAndSave(name: String, game: Game, signUps: SignUps? = null) {
        createIfNotExists(name)
        save(name, game)
        if (signUps !== null) {
            signUpDAO.createIfNotExists(name)
            saveSignUps(name, signUps)
        }
    }

    fun saveSignUps(name: String, signUps: SignUps) {
        signUpDAO.save(name, signUps)
    }

    inner class SignUpDAO: FileDAO<String, SignUps>() {
        override fun getPath(identifier: String): Path =
            gameDataPath.resolve(identifier).resolve("signUps.ser")
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
