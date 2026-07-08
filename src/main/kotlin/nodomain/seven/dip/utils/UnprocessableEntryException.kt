package nodomain.seven.dip.utils

import jakarta.ws.rs.core.Response
import jakarta.ws.rs.ext.ExceptionMapper
import jakarta.ws.rs.ext.Provider

class UnprocessableEntryException(message: String?, cause: Throwable? = null): RuntimeException(message, cause)

@Provider
class UnprocessableEntryExceptionMapper: ExceptionMapper<UnprocessableEntryException> {
    override fun toResponse(exception: UnprocessableEntryException) =
        Response.status(422).entity(mapOf(
                "error" to "Unprocessable Entity",
                "message" to (exception.message ?: "Input constraints not met")
        )).build()!!
}

