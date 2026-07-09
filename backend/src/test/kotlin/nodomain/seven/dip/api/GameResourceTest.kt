package nodomain.seven.dip.api

import kotlin.test.Test
import io.quarkus.test.junit.QuarkusTest
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.When
import io.restassured.module.kotlin.extensions.Then
import nodomain.seven.dip.utils.filePath
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.AfterAll
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.assertTrue

@QuarkusTest
class GameResourceTest {
    companion object{
        @JvmStatic
        @BeforeAll
        fun setFilePath() {
            filePath = filePath.resolve("test")
        }

        @JvmStatic
        @AfterAll
        fun cleanTestFolder() {
            assertTrue(filePath.endsWith("test"))
            Files.walk(filePath).use {
                it.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete)
            }
        }
    }

    @Test
    fun test() {
        Given {
            header("UserName", "someUnusedNameThatNoOneShallHaveClaimed12345")
            header("Password", "toShort")
        } When {
            put("api/game")
        } Then {
            statusCode(422)
        }
    }

    @Test
    fun happyPathSingleTurnRomans() {
        val gameId = "happy-path-game-test"

        Given {
            header("UserName", "t3st-GM")
            header("Password", "P-a-s-s-w-o-r-d")
        } When {
            put("api/game")
        } Then {
            statusCode(204)
        }

        Given {
            header("UserName", "t3st-GM")
            header("Password", "P-a-s-s-w-o-r-d")
            queryParam("id", gameId)
        } When {
            post("api/game")
        } Then {
            statusCode(200)
        }

        Given {
            header("UserName", "t3st-GM")
            header("Password", "P-a-s-s-w-o-r-d")
            queryParam("country", "cato")
        } When {
            post("api/game/$gameId")
        } Then {
            statusCode(200)
            body(equalTo("Cato"))
        }

        Given {
            header("UserName", "t3st-GM")
            header("Password", "P-a-s-s-w-o-r-d")
            body(GameResourceTest::class.java.getResource("/cato-test-orders.txt")!!.readText())
        } When {
            post("api/game/$gameId/cato")
        } Then {
            statusCode(200)
        }

        Given {
            header("UserName", "t3st-GM")
            header("Password", "P-a-s-s-w-o-r-d")
        } When {
            patch("api/game/$gameId")
        } Then {
            statusCode(200)
            body("turn", equalTo(2))
        }
    }
}
