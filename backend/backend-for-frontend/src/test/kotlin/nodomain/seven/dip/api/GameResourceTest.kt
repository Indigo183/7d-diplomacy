package nodomain.seven.dip.api

import kotlin.test.Test
import io.quarkus.test.junit.QuarkusTest
import io.restassured.http.ContentType
import io.restassured.module.kotlin.extensions.Extract
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.When
import io.restassured.module.kotlin.extensions.Then
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasSize

@QuarkusTest
class GameResourceTest {


    @Test
    fun happyPathSingleTurnRomans() {
        val gameId = "happy-path-game-test"

        val token = setupTestGame(gameId, startGame = true)

        Given {
            header("Authorization", "Bearer ${token.cato}")
            contentType(ContentType.TEXT)
            body(GameResourceTest::class.java.getResource("/cato-test-orders.txt")!!.readText())
        } When {
            post("api/game/$gameId/cato")
        } Then {
            statusCode(200)
        }

        Given {
            header("Authorization", "Bearer ${token.cato}")
            queryParam("ready", true)
        } When {
            post("api/game/$gameId/cato/ready")
        } Then {
            statusCode(204)
        }

        Given {
            header("Authorization", "Bearer ${token.pompey}")
            contentType(ContentType.JSON)
            body(GameResourceTest::class.java.getResource("/pompey-test-orders.json")!!.readText())
        } When {
            post("api/game/$gameId/pompey")
        } Then {
            statusCode(200)
        }

        Given {
            header("Authorization", "Bearer ${token.pompey}")
            queryParam("ready", true)
        } When {
            post("api/game/$gameId/pompey/ready")
        } Then {
            statusCode(204)
        }

        Given {
            header("Authorization", "Bearer ${token.cato}")
        } When {
            get("api/game/$gameId/cato/ready")
        } Then {
            statusCode(200)
            body(equalTo("true"))
        }

        println(Given {
            header("Authorization", "Bearer ${token.gm}")
        } When {
            patch("api/game/$gameId")
        } Then {
            statusCode(200)
            body("turn", equalTo(2))
        } Extract {
            body().asString()
        })
    }

    @Test
    fun tokenAccessTest() {
        val gameId = "token-access-log-test"

        val token = setupTestGame(gameId, startGame = false)

        Given {
            queryParam("country", "cato")
        } When {
            post("api/game/$gameId")
        } Then {
            statusCode(200)
            body(equalTo(token.cato))
        }

        Given {
            queryParam("country", "cato")
            queryParam("recovery-key", token.cato.substring(token.cato.length - 10))
        } When {
            post("api/game/$gameId")
        } Then {
            statusCode(200)
            body(equalTo(token.cato))
        }

        Given {
            header("Authorization", "Bearer ${token.cato}")
        } When {
            get("api/game/$gameId/cato/token-log")
        } Then {
            statusCode(200)
            body("token_created_log", hasSize<Long>(2))
            body("token_recovered_log", hasSize<Long>(1))
        }

        Given {
            header("Authorization", "Bearer ${token.gm}")
            queryParam("action", "set-property")
            queryParam("property", "started")
        } When {
            patch("api/game/$gameId")
        } Then {
            statusCode(200)
        }

        Given {
            queryParam("country", "cato")
        } When {
            post("api/game/$gameId")
        } Then {
            statusCode(403)
        }
    }

    @Test
    fun emptyOrderSerTest() {
        val gameId = "empty-order-set-test"

        val token = setupTestGame(gameId, startGame = false)

        Given {
            header("Authorization", "Bearer ${token.cato}")
        } When {
            get("api/game/$gameId/cato")
        } Then {
            statusCode(200)
            body(equalTo("[]"))
        }
    }
}

data class TestGameTokenSet(val gm: String, val cato: String, val pompey: String)

fun setupTestGame(gameId: String, startGame: Boolean = true): TestGameTokenSet {
    val gmToken = Given {
        queryParam("id", gameId)
    } When {
        post("api/game")
    } Then {
        statusCode(201)
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

    if (startGame) {
        Given {
            header("Authorization", "Bearer $gmToken")
            queryParam("action", "set-property")
            queryParam("property", "started")
        } When {
            patch("api/game/$gameId")
        } Then {
            statusCode(200)
        }
    }

    return TestGameTokenSet(gmToken, catoToken, pompeyToken)
}
