package nodomain.seven.dip.variant

import io.github.classgraph.ClassGraph
import io.quarkus.security.Authenticated
import io.quarkus.vertx.web.Route
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.FileSystemAccess
import io.vertx.ext.web.handler.StaticHandler
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.ws.rs.GET
import jakarta.ws.rs.PUT
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import org.eclipse.microprofile.config.inject.ConfigProperty
import java.security.MessageDigest
import kotlin.io.path.Path
import kotlin.io.path.readBytes
import kotlin.io.path.relativeTo
import kotlin.io.path.walk


data class VariantHash(val id: String, val hashes: MutableMap<String, String> = mutableMapOf())

@ApplicationScoped
class VariantRegister {
    val runtimeMapsDirectory = Path("extra_maps")
    val includedVariants = mutableMapOf<String, VariantHash>()

    private fun addHashToIncludedVariants(resourcePath: String, byteArray: ByteArray, digest: MessageDigest) {
        val mapName = resourcePath.substringBefore('/')
        includedVariants.putIfAbsent(mapName, VariantHash(mapName))
        includedVariants[mapName]?.hashes += resourcePath to String(digest.digest(byteArray))
    }

    fun includeRuntimeMaps(digest: MessageDigest = MessageDigest.getInstance("SHA-256")): MutableMap<String, VariantHash> {
        runtimeMapsDirectory.walk().forEach { addHashToIncludedVariants(
            it.relativeTo(runtimeMapsDirectory).toString().replace("\\", "/"),
            it.readBytes(), digest
        )}
        return includedVariants
    }

    init {
        val digest: MessageDigest = MessageDigest.getInstance("SHA-256")
        val resourcesPath = "META-INF/resources/maps"
        ClassGraph().enableNonSystemModules().enableClasspath()
            .acceptPaths(resourcesPath).scan().use {
                it.allResources.forEachByteArray { resource, bytes ->
                    addHashToIncludedVariants(resource.path.substringAfter("$resourcesPath/"), bytes, digest)
                }
            }
        includeRuntimeMaps(digest)
    }
}

@jakarta.ws.rs.Path("/variants")
class StaticFilesHashResource @Inject constructor(val variantRegister: VariantRegister) {
    @Produces(MediaType.APPLICATION_JSON)
    @GET
    fun allPrecomputedFileHashes(): Map<String, VariantHash> = variantRegister.includedVariants

    @Produces(MediaType.APPLICATION_JSON)
    @PUT
    fun recomputeFileHashes(): Map<String, VariantHash> = variantRegister.includeRuntimeMaps()
}

@ApplicationScoped
class StaticAuthenticatedDeclarativeRoute {
    @Route(path = "/maps/*", methods = [Route.HttpMethod.GET])
    @Authenticated
    fun secureStatic(rc: RoutingContext?) {
        StaticHandler.create(FileSystemAccess.RELATIVE, "extra_maps/").handle(rc)
    }
}
