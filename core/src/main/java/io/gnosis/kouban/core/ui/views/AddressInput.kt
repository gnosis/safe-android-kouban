package io.gnosis.kouban.core.ui.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import io.gnosis.kouban.core.R
import io.gnosis.kouban.core.databinding.AddressInputBinding
import io.gnosis.kouban.core.ui.helper.AddressHelper
import org.koin.core.KoinComponent
import org.koin.core.inject
import pm.gnosis.model.Solidity
import pm.gnosis.svalinn.common.utils.snackbar
import pm.gnosis.svalinn.common.utils.visible

class AddressInput
@JvmOverloads
constructor(
    context: Context, attrs: AttributeSet, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), KoinComponent {

    private val binding: AddressInputBinding

    init {
        with(context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater) {
            binding = AddressInputBinding.inflate(this, this@AddressInput)
        }
    }

    private lateinit var addressInputHelper: AddressInputHelper
    private val addressHelper: AddressHelper by inject()
    var onAddressChanged: ((Solidity.Address?) -> Unit)? = null

    var requestQRScanner: (() -> Unit)? = null

    fun setupInputHandler(showQr: Boolean) {
        addressInputHelper = AddressInputHelper(
            context,
            showQr,
            ::updateAddress,
            { requestQRScanner?.invoke() },
            {
                snackbar(binding.safeAddress, R.string.invalid_ethereum_address)
            })

        setOnClickListener {
            addressInputHelper.showDialog()
        }
    }

    fun updateAddress(address: Solidity.Address?, propagate: Boolean = true) {
        if (propagate) onAddressChanged?.invoke(address)
        if (address != null) {
            setAddress(address)
        } else {
            clearAddress()
        }
    }

    private fun setAddress(address: Solidity.Address) {
        binding.safeName.visible(false)
        binding.safeInputBackground.text = null
        binding.safeAddressInfo.text = null
        addressHelper.populateAddressInfo(binding.safeAddress, binding.safeImage, address)
    }

    private fun clearAddress() {
        binding.safeInputBackground.setCompoundDrawables(null, null, ContextCompat.getDrawable(context, R.drawable.ic_more_vert), null)
        binding.safeInputBackground.text = context.getString(R.string.address_hint)
        binding.safeName.visible(false)
        binding.safeName.text = null
        binding.safeAddress.text = null
        binding.safeImage.setAddress(null)
    }
}
