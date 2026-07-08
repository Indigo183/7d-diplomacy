package nodomain.seven.dip.api

import io.restassured.module.kotlin.extensions.Given
import kotlin.test.Test
import io.quarkus.test.junit.QuarkusTest
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When

@QuarkusTest
class GameResourceTest {

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
}
