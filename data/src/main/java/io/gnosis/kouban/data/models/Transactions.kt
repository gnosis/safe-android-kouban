package io.gnosis.kouban.data.models

import android.os.Parcelable
import io.gnosis.kouban.data.utils.SolidityAddressParceler
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.TypeParceler
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

@Parcelize
@TypeParceler<Solidity.Address, SolidityAddressParceler>
data class Transaction(
    val address: Solidity.Address,
    val timestamp: Long,
    val txHash: String?,
    val executionHash: String?,
    val transferInfo: TransferInfo?,
    val dataInfo: DataInfo?,
    val type: TransactionType,
    val state: TransactionState
) : Parcelable

@Parcelize
data class TransferInfo(
    val tokenSymbol: String,
    val amount: BigInteger,
    val decimals: Int,
    val tokenIconUrl: String?
) : Parcelable


@Parcelize
data class DataInfo(
    val ethValue: BigInteger,
    val dataByteLength: Int?,
    val methodName: String?
) : Parcelable
