package io.gnosis.kouban.data.models

import pm.gnosis.model.Solidity

data class TokenInfo(
    val address: Solidity.Address,
    val symbol: String,
    val decimals: Int,
    val name: String,
    val icon: String?
)
