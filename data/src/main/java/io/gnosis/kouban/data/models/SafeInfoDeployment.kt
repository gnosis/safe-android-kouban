package io.gnosis.kouban.data.models

import android.os.Parcel
import android.os.Parcelable
import pm.gnosis.model.Solidity
import pm.gnosis.utils.asEthereumAddress
import pm.gnosis.utils.asEthereumAddressString
import java.math.BigInteger

data class SafeInfoDeployment(
    val masterCopy: Solidity.Address,
    val fallbackHandler: Solidity.Address?,
    val owners: List<Solidity.Address>,
    val threshold: BigInteger
    //TODO: Add fields (payment, to, payment token, etc.)
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString()?.asEthereumAddress()!!,
        parcel.readString()?.asEthereumAddress(),
        parcel.createStringArrayList()?.map { it.asEthereumAddress()!! }!!.toList(),
        parcel.readInt().toBigInteger()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(masterCopy.asEthereumAddressString())
        parcel.writeString(fallbackHandler?.asEthereumAddressString())
        parcel.writeStringList(owners.map { it.asEthereumAddressString() })
        parcel.writeInt(threshold.toInt())
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SafeInfoDeployment> {
        override fun createFromParcel(parcel: Parcel): SafeInfoDeployment {
            return SafeInfoDeployment(parcel)
        }

        override fun newArray(size: Int): Array<SafeInfoDeployment?> {
            return arrayOfNulls(size)
        }
    }

}
