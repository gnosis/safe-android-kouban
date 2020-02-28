package io.gnosis.kouban.data.models

import pm.gnosis.model.Solidity
import java.math.BigInteger
import java.util.*

enum class TransactionState { Executed, Cancelled, Failed, Pending } //Confirmed?
enum class TransactionType { Incoming, Outgoing } // eventually add settings or other types
//enum class TransactionOperation { Call, Delegate }

data class TransactionsDto(
    val pending: List<Transaction>,
    val history: List<Transaction>
)

data class Transaction(
    val address: Solidity.Address,
    val timestamp: Long,
    //both null or both defined, must be filtered out
    val transferInfo: TransferInfo?,
    val dataInfo: DataInfo?,

    val type: TransactionType,
    val state: TransactionState
)

data class TransferInfo(
    val tokenSymbol: String,
    val amount: BigInteger,
    val decimals: Int,
    val tokenIconUrl: String?
)

data class DataInfo(
    val ethValue: BigInteger,
    val dataByteLength: Int?,
    val methodName: String?
)
