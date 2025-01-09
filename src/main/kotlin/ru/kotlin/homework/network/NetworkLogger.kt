@file:Suppress("unused")

package ru.kotlin.homework.network

import ru.kotlin.homework.Circle
import java.lang.IllegalArgumentException
import java.time.LocalDateTime

/**
 * Известный вам список ошибок
 */
sealed class ApiException(message: String) : Throwable(message) {
    data object NotAuthorized : ApiException("Not authorized") {
        private fun readResolve(): Any = NotAuthorized
    }

    data object NetworkException : ApiException("Not connected") {
        private fun readResolve(): Any = NetworkException
    }

    data object UnknownException: ApiException("Unknown exception") {
        private fun readResolve(): Any = UnknownException
    }
}

class ErrorLogger<E : Throwable> {

    private val errors = mutableListOf<Pair<LocalDateTime, E>>()

    fun log(response: NetworkResponse<*, E>) {
        if (response is Failure) {
            errors.add(response.responseDateTime to response.error)
        }
    }

    fun dumpLog() {
        errors.forEach { (date, error) ->
            println("Error at $date: ${error.message}")
        }
    }
}

fun processThrowables(logger: ErrorLogger<Throwable>) {
    logger.log(Success("Success"))
    Thread.sleep(100)
    logger.log(Success(Circle))
    Thread.sleep(100)
    logger.log(Failure(IllegalArgumentException("Something unexpected")))

    logger.dumpLog()
}

// Type mismatch: inferred type is ErrorLogger<Throwable> but ErrorLogger<ApiException> was expected
//   fix: logger экземпляр ErrorLogger<Throwable>, и на входе ожидается Throwable вместо ApiException
//        ApiException наследник Throwable, разрешение принимать в том числе и наследников дает модификатор "IN"
fun processApiErrors(apiExceptionLogger: ErrorLogger<in ApiException>) {
    apiExceptionLogger.log(Success("Success"))
    Thread.sleep(100)
    apiExceptionLogger.log(Success(Circle))
    Thread.sleep(100)
    apiExceptionLogger.log(Failure(ApiException.NetworkException))

    apiExceptionLogger.dumpLog()
}

fun main() {
    val logger = ErrorLogger<Throwable>()

    println("Processing Throwable:")
    processThrowables(logger)

    println("Processing Api:")
    processApiErrors(logger)
}

