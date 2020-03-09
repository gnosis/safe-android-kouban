package io.gnosis.kouban.push

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.inject
import timber.log.Timber

class KoubanFirebaseService : FirebaseMessagingService() {

    private val pushServiceRepo: PushServiceRepository by inject()
    private val prefs: PushPrefs by inject()

    override fun onMessageReceived(message: RemoteMessage) {
        // No data received
        if (message.data.isEmpty()) return
        try {
            pushServiceRepo.handlePushMessage(PushServiceRepository.PushMessage.fromMap(message.data))
        } catch (e: IllegalArgumentException) {
            Timber.e(e)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        runBlocking(Dispatchers.IO) {
            kotlin.runCatching {
                pushServiceRepo.registerDevice(token)
            }.onSuccess {
                prefs.token = token
            }.onFailure {
                prefs.token = null
            }
        }
    }
}
