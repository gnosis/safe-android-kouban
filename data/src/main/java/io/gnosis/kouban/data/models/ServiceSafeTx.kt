package io.gnosis.kouban.data.models

import pm.gnosis.model.Solidity
import java.util.*

data class ServiceSafeTx(
    val hash: String,
    val tx: SafeTx,
    val execInfo: SafeTxExecInfo,
    val confirmations: List<Pair<Solidity.Address, String?>>,
    val executed: Boolean,
    val executionDate: Date?,
    val submissionDate: Date?,
    val txHash: String?
)
