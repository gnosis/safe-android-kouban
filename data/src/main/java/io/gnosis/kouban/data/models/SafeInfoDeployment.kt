package io.gnosis.kouban.data.models

import pm.gnosis.model.Solidity
import java.math.BigInteger

data class SafeInfoDeployment(
    val masterCopy: Solidity.Address,
    val fallbackHandler: Solidity.Address,
    val owners: List<Solidity.Address>,
    val threshold: BigInteger
    //TODO: Add fields (payment, to, payment token, etc.)
)
