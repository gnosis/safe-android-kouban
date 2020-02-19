package io.gnosis.kouban.data.models

import pm.gnosis.model.Solidity

data class ServiceSafeTx(
    val hash: String,
    val tx: SafeTx,
    val execInfo: SafeTxExecInfo,
    val confirmations: List<Pair<Solidity.Address, String?>>,
    val executed: Boolean,
    val txHash: String?
)
