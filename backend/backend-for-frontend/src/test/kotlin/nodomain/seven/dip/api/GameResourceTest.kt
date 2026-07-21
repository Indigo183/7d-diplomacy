package nodomain.seven.dip.api

import kotlin.test.Test
import io.quarkus.test.junit.QuarkusTest
import io.restassured.module.kotlin.extensions.Extract
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.When
import io.restassured.module.kotlin.extensions.Then
import nodomain.seven.dip.utils.filePath
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.AfterAll
import java.nio.file.Files
import java.util.Comparator.reverseOrder
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
            if (Files.exists(filePath)) {
                Files.walk(filePath).use {
                    it.sorted(reverseOrder()).forEach(Files::delete)
                }
            }
        }
    }

    @Test
    fun happyPathSingleTurnRomans() {
        val gameId = "happy-path-game-test"

        val gmToken = Given {
            queryParam("id", gameId)
        } When {
            post("api/game")
        } Then {
            statusCode(200)
        } Extract {
            body().asString()
        }

        val catoToken = Given {
            queryParam("country", "cato")
        } When {
            post("api/game/$gameId")
        } Then {
            statusCode(200)
        } Extract {
            body().asString()
        }

        val pompeyToken = Given {
            queryParam("country", "pompey")
        } When {
            post("api/game/$gameId")
        } Then {
            statusCode(200)
        } Extract {
            body().asString()
        }

        Given {
            header("Authorisation", "BEARER $catoToken")
            body(GameResourceTest::class.java.getResource("/cato-test-orders.txt")!!.readText())
        } When {
            post("api/game/$gameId/cato")
        } Then {
            statusCode(200)
        }

        Given {
            header("Authorisation", "BEARER $catoToken")
            queryParam("ready", true)
        } When {
            post("api/game/$gameId/cato/ready")
        } Then {
            statusCode(204)
        }

        Given {
            header("Authorisation", "BEARER $pompeyToken")
            body(GameResourceTest::class.java.getResource("/pompey-test-orders.txt")!!.readText())
        } When {
            post("api/game/$gameId/pompey")
        } Then {
            statusCode(200)
        }

        Given {
            header("Authorisation", "BEARER $pompeyToken")
            queryParam("ready", true)
        } When {
            post("api/game/$gameId/pompey/ready")
        } Then {
            statusCode(204)
        }

        Given {
            header("Authorisation", "BEARER $catoToken")
        } When {
            get("api/game/$gameId/cato/ready")
        } Then {
            statusCode(200)
            body(equalTo("true"))
        }

        Given {
            header("Authorisation", "BEARER $gmToken")
        } When {
            patch("api/game/$gameId")
        } Then {
            statusCode(200)
            body("turn", equalTo(2))
        }
    }
}
