package io.gnosis.kouban.data.models

import android.os.Parcelable
import io.gnosis.kouban.data.utils.SolidityAddressParceler
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.TypeParceler
import pm.gnosis.model.Solidity
import java.math.BigInteger

@Parcelize
@TypeParceler<Solidity.Address, SolidityAddressParceler>
data class SafeTxExecInfo(
    val baseGas: BigInteger,
    val txGas: BigInteger,
    val gasPrice: BigInteger,
    val gasToken: Solidity.Address,
    val refundReceiver: Solidity.Address,
    val nonce: BigInteger
) : Parcelable {
    @IgnoredOnParcel
    val fees by lazy { (baseGas + txGas) * gasPrice }
}
