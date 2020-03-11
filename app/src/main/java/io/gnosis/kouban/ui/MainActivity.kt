package io.gnosis.kouban.ui

import android.os.Bundle
import io.gnosis.kouban.core.ui.base.BaseActivity
import io.gnosis.kouban.databinding.ActivityMainBinding
import io.gnosis.kouban.push.PushServiceRepository
import org.koin.android.ext.android.inject

class MainActivity : BaseActivity() {

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val pushRepo: PushServiceRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }

    override fun onResume() {
        super.onResume()
        pushRepo.checkRegistration()
    }
}
