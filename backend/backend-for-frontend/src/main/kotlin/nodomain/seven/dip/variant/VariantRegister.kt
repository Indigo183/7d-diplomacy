package nodomain.seven.dip.variant

import io.github.classgraph.ClassGraph
import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.config.inject.ConfigProperty
import jakarta.inject.Inject
import jakarta.ws.rs.Produces
import jakarta.ws.rs.GET
import jakarta.ws.rs.PUT
import jakarta.ws.rs.core.MediaType
import java.security.MessageDigest
import kotlin.io.path.Path
import kotlin.io.path.readBytes
import kotlin.io.path.relativeTo
import kotlin.io.path.walk

data class VariantHash(val id: String, val hashes: MutableMap<String, String> = mutableMapOf())

@ApplicationScoped
class VariantRegister @Inject constructor(@ConfigProperty(name = "quarkus.http.static-dir.path") runtimeMapsDirectory: String) {
    val runtimeMapsDirectory = Path(runtimeMapsDirectory)
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
    val digest: MessageDigest = MessageDigest.getInstance("SHA-256")

    @Produces(MediaType.APPLICATION_JSON)
    @GET
    fun allPrecomputedFileHashes(): Map<String, VariantHash> = variantRegister.includedVariants

    @Produces(MediaType.APPLICATION_JSON)
    @PUT
    fun recomputeFileHashes(): Map<String, VariantHash> = variantRegister.includeRuntimeMaps()
}
