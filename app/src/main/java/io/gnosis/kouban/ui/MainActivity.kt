package io.gnosis.kouban.ui

import android.os.Bundle
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import io.gnosis.kouban.push.PushPrefs
import io.gnosis.kouban.databinding.ActivityMainBinding
import io.gnosis.kouban.core.ui.base.BaseActivity
import io.gnosis.kouban.push.PushServiceRepository
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.inject

class MainActivity : BaseActivity() {

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val prefs: PushPrefs by inject()
    private val pushServiceRepo: PushServiceRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }

    override fun onResume() {
        super.onResume()
        if (!prefs.isDeviceRegistered) {
            FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    return@OnCompleteListener
                }
                // Get new Instance ID token
                val token = task.result?.token
                token?.let { token ->
                    runBlocking {
                        kotlin.runCatching {
                            pushServiceRepo.registerDevice(token)
                        }
                            .onSuccess { prefs.token = token }
                            .onFailure { prefs.token = null }
                    }
                }
            })
        }
    }
}
