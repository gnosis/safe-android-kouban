package io.gnosis.kouban.data.models

import pm.gnosis.model.Solidity
import java.math.BigInteger

data class SafeInfo(
    val address: Solidity.Address,
    val masterCopy: Solidity.Address,
    val fallbackHandler: Solidity.Address?,
    val owners: List<Solidity.Address>,
    val threshold: BigInteger,
    val currentNonce: BigInteger,
    val modules: List<Solidity.Address>
)
