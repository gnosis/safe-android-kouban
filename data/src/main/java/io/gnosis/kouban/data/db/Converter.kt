package io.gnosis.kouban.data.db

import androidx.room.TypeConverter
import pm.gnosis.model.Solidity
import pm.gnosis.utils.asEthereumAddress
import pm.gnosis.utils.asEthereumAddressString
import pm.gnosis.utils.hexAsBigIntegerOrNull
import pm.gnosis.utils.toHexString
import java.math.BigInteger

class SolidityAddressConverter {
    @TypeConverter
    fun fromHexString(address: String) = address.asEthereumAddress()!!

    @TypeConverter
    fun toHexString(address: Solidity.Address): String = address.asEthereumAddressString()
}

class BigIntegerConverter {
    @TypeConverter
    fun fromHexString(hexString: String?) = hexString?.hexAsBigIntegerOrNull()

    @TypeConverter
    fun toHexString(value: BigInteger?): String? = value?.toHexString()
}

