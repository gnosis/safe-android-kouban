package io.gnosis.kouban.core.ui.views


import android.content.ClipboardManager
import android.content.Context
import android.view.LayoutInflater
import com.google.android.material.bottomsheet.BottomSheetDialog
import io.gnosis.kouban.core.R
import io.gnosis.kouban.core.utils.parseEthereumAddress
import io.gnosis.kouban.core.databinding.BottomSheetAddressInputBinding
import kotlinx.android.synthetic.main.bottom_sheet_address_input.*
import pm.gnosis.model.Solidity
import pm.gnosis.svalinn.common.utils.toast
import pm.gnosis.svalinn.common.utils.visible

class AddressInputHelper(
    context: Context,
    showQr: Boolean,
    private val addressCallback: (Solidity.Address, Boolean) -> Unit,
    private val qrCallback: () -> Unit,
    private val errorCallback: ((Throwable) -> Unit)? = null
) {

    private val binding: BottomSheetAddressInputBinding by lazy {
        BottomSheetAddressInputBinding.inflate(LayoutInflater.from(context))
    }

    private val dialog =
        BottomSheetDialog(context).apply {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

            setContentView(binding.root)

            if (!showQr) {
                binding.bottomSheetAddressInputQr.visible(showQr)
                binding.bottomSheetAddressInputQrIcon.visible(showQr)
                binding.bottomSheetAddressInputQrTouch.visible(showQr)
            }

            bottom_sheet_address_input_qr_touch.setOnClickListener {
                qrCallback()
                hide()
            }
            bottom_sheet_address_input_paste_touch.setOnClickListener {
                (clipboard.primaryClip?.getItemAt(0)?.text?.let { parseEthereumAddress(it.toString()) }
                    ?: run {
                        handleError(IllegalArgumentException("No Ethereum address found"))
                        null
                    })?.let { addressCallback(it, true) }
                hide()
            }
        }

    fun showDialog() = dialog.show()

    fun hideDialog() = dialog.hide()

    private fun handleError(t: Throwable) {
        errorCallback?.invoke(t) ?: dialog.context.toast(R.string.invalid_ethereum_address)
    }
}
