package br.com.zup.edu.cria.exceptions

import br.com.zup.edu.exceptions.ExceptionHandler
import io.grpc.Status
import javax.inject.Singleton

@Singleton
class ChavePixInvalidaExceptionHandler : ExceptionHandler<ChavePixInvalidaException> {

    override fun handle(e: ChavePixInvalidaException): ExceptionHandler.StatusWithDetails {
        return ExceptionHandler.StatusWithDetails(
            Status.INVALID_ARGUMENT
                .withDescription(e.message)
                .withCause(e)
        )
    }

    override fun supports(e: Exception): Boolean {
        return e is ChavePixInvalidaException
    }
}