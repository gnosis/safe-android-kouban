package io.gnosis.kouban.data.utils

import pm.gnosis.utils.asEthereumAddress
import timber.log.Timber

//Map functions that throw exceptions into optional types
inline fun <T> nullOnThrow(func: () -> T): T? = try {
    func.invoke()
} catch (e: Exception) {
    null
}

//Map functions that throw exceptions into optional types
inline fun loggedTry(func: () -> Unit) = try {
    func()
} catch (e: Exception) {
    Timber.e(e)
}

fun String.parseEthereumAddress() =
    removePrefix("ethereum:").asEthereumAddress()

