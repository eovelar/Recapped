package com.recapped.app.domain

/**
 * Resultado genérico expuesto desde la capa de datos a la UI.
 * Modela explícitamente los tres estados que pide la consigna:
 * Loading / Success / Error.
 */
sealed interface Resource<out T> {
    data object Loading : Resource<Nothing>
    data class Success<T>(val data: T) : Resource<T>
    data class Error(val message: String, val cause: Throwable? = null) : Resource<Nothing>
}
