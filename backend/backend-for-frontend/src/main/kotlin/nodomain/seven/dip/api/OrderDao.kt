package nodomain.seven.dip.api

import jakarta.enterprise.context.Dependent
import jakarta.inject.Inject
import nodomain.seven.dip.file.FileDAO
import nodomain.seven.dip.file.FilePathService
import nodomain.seven.dip.orders.Inputtable
import java.io.Serializable
import java.nio.file.Path
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@JvmInline
value class OrderWriteUp(val orders: List<Inputtable>): Serializable

@Dependent
class OrderDao @Inject constructor(
    val tokenAccessDAO: TokenAccessDAO,
    val filePathService: FilePathService
): FileDAO<String, OrderWriteUp>() {
    lateinit var orderFilePath: Path

    fun with(gameId: String): OrderDao {
        orderFilePath = filePathService.countryDataDirectory(gameId)
        tokenAccessDAO.with(gameId)
        return this
    }

    override fun getPath(identifier: String): Path =
        orderFilePath.resolve(identifier).resolve("currentOrders.ser")

    override fun onCreation(identifier: String, creationPath: Path) {
        tokenAccessDAO.createIfNotExists(identifier)
        tokenAccessDAO.save(identifier, TokenAccess())
    }
}

data class TokenAccess(
    val tokenCreatedLog: MutableList<Long> = mutableListOf(),
    val tokenRecoveredLog: MutableList<Long> = mutableListOf()
): Serializable

@Dependent
class TokenAccessDAO @Inject constructor(val filePathService: FilePathService): FileDAO<String, TokenAccess>() {
    lateinit var orderFilePath: Path

    fun with(gameId: String): TokenAccessDAO {
        orderFilePath = filePathService.countryDataDirectory(gameId)
        return this
    }

    override fun getPath(identifier: String): Path =
        orderFilePath.resolve(identifier).resolve("tokenLog.ser")

    @OptIn(ExperimentalTime::class)
    fun logCreateToken(country: String) {
        val log = load(country)
        log.tokenCreatedLog += Clock.System.now().epochSeconds
        save(country, log)
    }

    @OptIn(ExperimentalTime::class)
    fun logRecoverToken(country: String) {
        val log = load(country)
        log.tokenRecoveredLog += Clock.System.now().epochSeconds
        save(country, log)
    }

}
