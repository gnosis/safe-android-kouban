package io.gnosis.kouban.safe_check.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import io.gnosis.kouban.core.ui.base.BaseDialogFragment
import io.gnosis.kouban.safe_check.databinding.DialogSafeDeploymentDetailsBinding

class SafeDeploymentDetailsDialog : BaseDialogFragment<DialogSafeDeploymentDetailsBinding>() {

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): DialogSafeDeploymentDetailsBinding =
        DialogSafeDeploymentDetailsBinding.inflate(inflater, container, false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    companion object {

        private val TAG = SafeDeploymentDetailsDialog::class.java.simpleName

        fun show(context: Context) {
            SafeDeploymentDetailsDialog().show((context as FragmentActivity).supportFragmentManager, TAG)
        }
    }
}
