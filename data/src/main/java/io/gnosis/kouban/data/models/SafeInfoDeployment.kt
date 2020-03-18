package io.gnosis.kouban.data.models

import android.os.Parcelable
import io.gnosis.kouban.data.utils.SolidityAddressParceler
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.TypeParceler
import pm.gnosis.model.Solidity
import java.math.BigInteger

@Parcelize
@TypeParceler<Solidity.Address, SolidityAddressParceler>
data class SafeInfoDeployment(
    val txHash: String,
    val masterCopy: Solidity.Address,
    val fallbackHandler: Solidity.Address,
    val owners: List<Solidity.Address>,
    val threshold: BigInteger
    //TODO: Add fields (payment, to, payment token, etc.)
) : Parcelable
