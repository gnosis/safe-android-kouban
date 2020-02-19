package io.gnosis.kouban.data.utils

import android.graphics.Bitmap
import android.graphics.Color
import pm.gnosis.crypto.utils.asEthereumAddressChecksumString
import pm.gnosis.model.Solidity

fun String.asMiddleEllipsized(boundariesLength: Int): String {
    return if (this.length > boundariesLength * 2)
        "${this.subSequence(0, boundariesLength)}...${this.subSequence(this.length - boundariesLength, this.length)}"
    else this
}

fun Solidity.Address.shortChecksumString() =
    asEthereumAddressChecksumString().asMiddleEllipsized(4)

