package nodomain.seven.dip.utils.exceptions

import jakarta.ws.rs.core.Response
import jakarta.ws.rs.ext.ExceptionMapper
import jakarta.ws.rs.ext.Provider

class UnauthenticatedException(message: String?, cause: Throwable? = null): RuntimeException(message, cause)

@Provider
class UnauthenticatedExceptionMapper: ExceptionMapper<UnauthenticatedException> {
    override fun toResponse(exception: UnauthenticatedException) =
        Response.status(401).entity(mapOf(
            "error" to "Unauthenticated",
            "message" to (exception.message ?: "Unknown source")
        )).build()!!
}
