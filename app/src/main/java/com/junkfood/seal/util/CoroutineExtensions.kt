package com.junkfood.seal.util

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Extension function to safely launch coroutines with exception handling
 */
fun CoroutineScope.launchSafely(
    onError: ((Throwable) -> Unit)? = null,
    block: suspend CoroutineScope.() -> Unit
): Job {
    val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        onError?.invoke(throwable) ?: throwable.printStackTrace()
    }
    
    return this.launch(exceptionHandler) {
        block()
    }
}
