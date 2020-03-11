package io.gnosis.kouban.push

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.koin.android.ext.android.inject
import timber.log.Timber

class KoubanFirebaseService : FirebaseMessagingService() {

    private val pushServiceRepo: PushServiceRepository by inject()

    override fun onMessageReceived(message: RemoteMessage) {
        message.data.takeUnless { it.isNullOrEmpty() }?.let {
            runCatching {
                pushServiceRepo.handlePushMessage(PushServiceRepository.PushMessage.fromMap(it))

            }.onFailure(Timber::e)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        pushServiceRepo.registerDevice(token)
    }
}
