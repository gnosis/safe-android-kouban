package io.gnosis.kouban.push

import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import io.gnosis.kouban.core.managers.SafeAddressManager
import io.gnosis.kouban.data.backend.PushServiceApi
import io.gnosis.kouban.helpers.LocalNotificationManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import pm.gnosis.model.Solidity
import pm.gnosis.utils.asEthereumAddressString

class PushServiceRepository(
    private val localNotificationManager: LocalNotificationManager,
    private val pushServiceApi: PushServiceApi,
    private val prefs: PushPrefs,
    private val safeAddressManager: SafeAddressManager
) {

    fun checkRegistration() {

        if (!prefs.isDeviceRegistered) {
            FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    return@OnCompleteListener
                }
                // Get new Instance ID token
                val token = task.result?.token
                token?.let { token ->
                    runBlocking(Dispatchers.IO) {
                        kotlin.runCatching {
                            registerDevice(token)
                        }
                            .onSuccess { prefs.token = token }
                            .onFailure { prefs.token = null }
                    }
                }
            })

        } else {

            if (!prefs.isSafeRegistered) {

                runBlocking(Dispatchers.IO) {
                    safeAddressManager.getSafeAddress()?.let { safe ->
                        kotlin.runCatching {
                            registerSafe(safe)
                        }
                            .onSuccess { prefs.safe = safe }
                            .onFailure { prefs.token = null }
                    }
                }
            }
        }
    }

    fun registerDevice(token: String) {
        runBlocking(Dispatchers.IO) {
            kotlin.runCatching {
                pushServiceApi.registerDevice(PushServiceApi.DeviceRegistration(prefs.clientId, token))
            }.onSuccess {
                prefs.token = token
            }.onFailure {
                prefs.token = null
            }
        }
    }

    fun registerSafe(safe: Solidity.Address) {
        runBlocking(Dispatchers.IO) {
            kotlin.runCatching {
                pushServiceApi.registerPushes("rinkeby", safe.asEthereumAddressString(), PushServiceApi.PushesRegistration(prefs.clientId))
            }.onSuccess {
                prefs.safe = safe
            }.onFailure {
                prefs.safe = null
            }
        }
    }

    fun handlePushMessage(pushMessage: PushMessage) {

    }

    sealed class PushMessage(
        val type: String
    ) {
        data class SendTransaction(
            val hash: String,
            val safe: String,
            val to: String,
            val value: String,
            val data: String,
            val operation: String,
            val txGas: String,
            val dataGas: String,
            val operationalGas: String,
            val gasPrice: String,
            val gasToken: String,
            val nonce: String,
            val r: String,
            val s: String,
            val v: String
        ) : PushMessage(TYPE) {
            companion object {
                const val TYPE = "sendTransaction"
                fun fromMap(params: Map<String, String>) =
                    SendTransaction(
                        params.getOrThrow("hash"),
                        params.getOrThrow("safe"),
                        params.getOrThrow("to"),
                        params.getOrThrow("value"),
                        params.getOrThrow("data"),
                        params.getOrThrow("operation"),
                        params.getOrThrow("txGas"),
                        params.getOrThrow("dataGas"),
                        params.getOrThrow("operationalGas"),
                        params.getOrThrow("gasPrice"),
                        params.getOrThrow("gasToken"),
                        params.getOrThrow("nonce"),
                        params.getOrThrow("r"),
                        params.getOrThrow("s"),
                        params.getOrThrow("v")
                    )
            }
        }

        data class ConfirmTransaction(
            val hash: String,
            val r: String,
            val s: String,
            val v: String
        ) : PushMessage(TYPE) {
            companion object {
                const val TYPE = "confirmTransaction"
                fun fromMap(params: Map<String, String>) =
                    ConfirmTransaction(
                        params.getOrThrow("hash"),
                        params.getOrThrow("r"),
                        params.getOrThrow("s"),
                        params.getOrThrow("v")
                    )
            }
        }

        data class RejectTransaction(
            val hash: String,
            val r: String,
            val s: String,
            val v: String
        ) : PushMessage(TYPE) {
            companion object {
                const val TYPE = "rejectTransaction"
                fun fromMap(params: Map<String, String>) =
                    RejectTransaction(
                        params.getOrThrow("hash"),
                        params.getOrThrow("r"),
                        params.getOrThrow("s"),
                        params.getOrThrow("v")
                    )
            }
        }

        companion object {
            fun fromMap(params: Map<String, String>) =
                when (params["type"]) {
                    "sendTransaction" -> SendTransaction.fromMap(
                        params
                    )
                    "confirmTransaction" -> ConfirmTransaction.fromMap(
                        params
                    )
                    "rejectTransaction" -> RejectTransaction.fromMap(
                        params
                    )
                    else -> throw IllegalArgumentException("Unknown push type")
                }
        }
    }
}

private fun Map<String, String>.getOrThrow(key: String) =
    get(key) ?: throw IllegalArgumentException("Missing param $key")
