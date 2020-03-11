package io.gnosis.kouban.repositories

import io.gnosis.kouban.Prefs
import io.gnosis.kouban.data.backend.PushServiceApi
import io.gnosis.kouban.helpers.LocalNotificationManager
import pm.gnosis.model.Solidity
import pm.gnosis.utils.asEthereumAddressString

class PushServiceRepository(
    private val localNotificationManager: LocalNotificationManager,
    private val pushServiceApi: PushServiceApi,
    private val prefs: Prefs
) {

    suspend fun registerDevice(token: String) {
        pushServiceApi.registerDevice(PushServiceApi.DeviceRegistration(prefs.clientId, token))
    }

    suspend fun registerPushes(safe: Solidity.Address) {
        pushServiceApi.registerPushes("rinkeby", safe.asEthereumAddressString(), PushServiceApi.PushesRegistration(prefs.clientId))
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
                    "sendTransaction" -> SendTransaction.fromMap(params)
                    "confirmTransaction" -> ConfirmTransaction.fromMap(params)
                    "rejectTransaction" -> RejectTransaction.fromMap(params)
                    else -> throw IllegalArgumentException("Unknown push type")
                }
        }
    }
}

private fun Map<String, String>.getOrThrow(key: String) =
    get(key) ?: throw IllegalArgumentException("Missing param $key")
