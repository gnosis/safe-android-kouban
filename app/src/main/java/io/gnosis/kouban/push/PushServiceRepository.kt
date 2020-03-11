package io.gnosis.kouban.push

import android.content.Context
import androidx.navigation.NavDeepLinkBuilder
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import io.gnosis.kouban.R
import io.gnosis.kouban.core.managers.SafeAddressManager
import io.gnosis.kouban.data.BuildConfig
import io.gnosis.kouban.data.backend.PushServiceApi
import io.gnosis.kouban.helpers.LocalNotificationManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import pm.gnosis.model.Solidity
import pm.gnosis.utils.asEthereumAddressString

class PushServiceRepository(
    private val context: Context,
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
                token?.let {
                    registerDevice(it)
                    checkSafeRegistration()
                }
            })
        } else {
            checkSafeRegistration()
        }
    }

    private fun checkSafeRegistration() {
        if (!prefs.isSafeRegistered) {
            runBlocking(Dispatchers.IO) {
                safeAddressManager.getSafeAddress()?.let {
                    registerSafe(it)
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
                pushServiceApi.registerPushes(BLOCKCHAIN_NETWORK, safe.asEthereumAddressString(), PushServiceApi.PushesRegistration(prefs.clientId))
            }.onSuccess {
                prefs.safe = safe
            }.onFailure {
                prefs.safe = null
            }
        }
    }

    //TODO: different handling of incomming notifications based on type
    fun handlePushMessage(pushMessage: PushMessage) {
        showTransactionNotification(pushMessage)
    }

    private fun showTransactionNotification(pushMessage: PushMessage) {
        pushMessage.apply {
            //TODO: get transaction from push message
            val transaction = null //Transaction

            val intent =
                NavDeepLinkBuilder(context).setGraph(R.navigation.main_nav_graph).setDestination(R.id.transactionDetailsFragment).setArguments(null)
                    .createPendingIntent()

            localNotificationManager.show(
                context,
                context.getString(R.string.push_tex_title),
                context.getString(R.string.push_tx_message)
            )
        }
    }


    //TODO: get all relevant fields
    sealed class PushMessage(
        val type: String
    ) {

        data class NewConfirmation(
            val address: String
        ) : PushMessage(TYPE) {
            companion object {
                const val TYPE = "NEW_CONFIRMATION"
                fun fromMap(params: Map<String, String>) =
                    NewConfirmation(
                        params.getOrThrow("address")
                    )
            }
        }

        data class PendingMultisigTransaction(
            val address: String
        ) : PushMessage(TYPE) {
            companion object {
                const val TYPE = "PENDING_MULTISIG_TRANSACTION"
                fun fromMap(params: Map<String, String>) =
                    PendingMultisigTransaction(
                        params.getOrThrow("address")
                    )
            }
        }

        data class ExecutedMultisigTransaction(
            val address: String
        ) : PushMessage(TYPE) {
            companion object {
                const val TYPE = "EXECUTED_MULTISIG_TRANSACTION"
                fun fromMap(params: Map<String, String>) =
                    ExecutedMultisigTransaction(
                        params.getOrThrow("address")
                    )
            }
        }

        data class IncomingEther(
            val address: String
        ) : PushMessage(TYPE) {
            companion object {
                const val TYPE = "INCOMING_ETHER"
                fun fromMap(params: Map<String, String>) =
                    IncomingEther(
                        params.getOrThrow("address")
                    )
            }
        }

        data class IncomingToken(
            val address: String
        ) : PushMessage(TYPE) {
            companion object {
                const val TYPE = "INCOMING_TOKEN"
                fun fromMap(params: Map<String, String>) =
                    IncomingToken(
                        params.getOrThrow("address")
                    )
            }
        }


        companion object {
            fun fromMap(params: Map<String, String>) =
                when (params["type"]) {
                    NewConfirmation.TYPE -> NewConfirmation.fromMap(params)
                    PendingMultisigTransaction.TYPE -> PendingMultisigTransaction.fromMap(params)
                    ExecutedMultisigTransaction.TYPE -> ExecutedMultisigTransaction.fromMap(params)
                    IncomingEther.TYPE -> IncomingEther.fromMap(params)
                    IncomingToken.TYPE -> IncomingToken.fromMap(params)
                    else -> throw IllegalArgumentException("Unknown push type")
                }
        }
    }

    companion object {
        val BLOCKCHAIN_NETWORK = if (BuildConfig.BLOCKCHAIN_CHAIN_ID == 1L) "mainnet" else "rinkeby"
    }
}

private fun Map<String, String>.getOrThrow(key: String) =
    get(key) ?: throw IllegalArgumentException("Missing param $key")
