package nodomain.seven.dip.utils.exceptions

import jakarta.ws.rs.core.Response
import jakarta.ws.rs.ext.ExceptionMapper
import jakarta.ws.rs.ext.Provider

class ConflictException(message: String?, cause: Throwable? = null): RuntimeException(message, cause)

@Provider
class ConflictExceptionMapper: ExceptionMapper<ConflictException> {
    override fun toResponse(exception: ConflictException) =
        Response.status(409).entity(mapOf(
            "error" to "Conflict",
            "message" to (exception.message ?: "Already exists")
        )).build()!!
}
