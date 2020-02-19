package io.gnosis.kouban.core.utils

import pm.gnosis.crypto.utils.asEthereumAddressChecksumString
import pm.gnosis.model.Solidity

fun Solidity.Address.shortChecksumString() =
    asEthereumAddressChecksumString().asMiddleEllipsized(4)
