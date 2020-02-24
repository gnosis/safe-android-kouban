package io.gnosis.kouban.ui

import android.os.Bundle
import io.gnosis.kouban.core.databinding.ActivityMainBinding
import io.gnosis.kouban.core.ui.base.BaseActivity

class MainActivity : BaseActivity() {

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }
}
