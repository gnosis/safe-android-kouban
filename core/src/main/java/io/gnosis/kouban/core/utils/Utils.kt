package io.gnosis.kouban.core.utils

import android.content.Context
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import io.gnosis.kouban.core.R
import pm.gnosis.model.Solidity
import pm.gnosis.svalinn.common.utils.getColorCompat
import pm.gnosis.svalinn.utils.ethereum.ERC67Parser
import pm.gnosis.utils.asEthereumAddress
import pm.gnosis.utils.asEthereumAddressString

fun parseEthereumAddress(address: String) = address.asEthereumAddress() ?: ERC67Parser.parse(address)?.address

fun Solidity.Address.formatEthAddress(context: Context): Spannable {
    //make first & last 4 characters black
    val addressString = SpannableStringBuilder(this.asEthereumAddressString()).insert(21, "\n")
    addressString.setSpan(ForegroundColorSpan(context.getColorCompat(R.color.address_boundaries)), 0, 4, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    addressString.setSpan(ForegroundColorSpan(context.getColorCompat(R.color.address_boundaries)), addressString.length - 4, addressString.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    return addressString
}
