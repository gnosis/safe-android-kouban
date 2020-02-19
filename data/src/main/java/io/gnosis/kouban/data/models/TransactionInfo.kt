package io.gnosis.kouban.data.models

import pm.gnosis.model.Solidity

data class TransactionInfo(
    val recipient: Solidity.Address,
    val recipientLabel: String,
    val assetIcon: String?,
    val assetLabel: String,
    val additionalInfo: String? = null
)
