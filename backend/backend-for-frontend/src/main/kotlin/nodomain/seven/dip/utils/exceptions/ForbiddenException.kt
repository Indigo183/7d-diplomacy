package nodomain.seven.dip.utils.exceptions

import jakarta.ws.rs.core.Response
import jakarta.ws.rs.ext.ExceptionMapper
import jakarta.ws.rs.ext.Provider

class ForbiddenException(message: String?, cause: Throwable? = null): RuntimeException(message, cause)

@Provider
class ForbiddenExceptionMapper: ExceptionMapper<ForbiddenException> {
    override fun toResponse(exception: ForbiddenException) =
        Response.status(403).entity(mapOf(
            "error" to "Unauthorised",
            "message" to (exception.message ?: "Already exists")
        )).build()!!
}