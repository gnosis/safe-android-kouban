package io.gnosis.kouban.core.ui.helper


import android.widget.TextView
import io.gnosis.kouban.core.ui.views.AddressTooltip
import io.gnosis.kouban.core.utils.shortChecksumString
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import pm.gnosis.blockies.BlockiesImageView
import pm.gnosis.crypto.utils.asEthereumAddressChecksumString
import pm.gnosis.model.Solidity

class AddressHelper {

    fun populateAddressInfo(
        addressView: TextView,
        imageView: BlockiesImageView?,
        address: Solidity.Address
    ) {

        GlobalScope.launch(Dispatchers.Default) {
            val (displayAddress, fullAddress) = address.shortChecksumString() to address.asEthereumAddressChecksumString()

            addressView.post {
                imageView?.setAddress(address)
                addressView.text = displayAddress
                addressView.setOnClickListener {
                    AddressTooltip(addressView.context, fullAddress).showAsDropDown(addressView)
                }
            }
        }
    }
}
