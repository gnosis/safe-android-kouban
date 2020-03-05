package io.gnosis.kouban.data.models

import android.os.Parcelable
import io.gnosis.kouban.data.utils.SolidityAddressParceler
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.TypeParceler
import pm.gnosis.model.Solidity
import java.math.BigInteger


@Parcelize
@TypeParceler<Solidity.Address, SolidityAddressParceler>
data class SafeTx(
    val to: Solidity.Address,
    val value: BigInteger,
    val data: String,
    val operation: Operation
) : Parcelable {

    enum class Operation(val id: Int) {
        CALL(0),
        DELEGATE(1)
    }
}
