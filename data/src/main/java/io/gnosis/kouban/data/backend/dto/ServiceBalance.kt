package io.gnosis.kouban.data.backend.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import io.gnosis.kouban.data.backend.adapter.DecimalNumber
import pm.gnosis.model.Solidity
import java.math.BigInteger

@JsonClass(generateAdapter = true)
data class ServiceBalance(
    @Json(name = "tokenAddress") val tokenAddress: Solidity.Address?,
    @Json(name = "token") val token: ServiceTokenMeta?,
    @DecimalNumber @Json(name = "balance") val balance: BigInteger,
    @Json(name = "balanceUsd") val balanceUsd: String?
) {
    @JsonClass(generateAdapter = true)
    data class ServiceTokenMeta(
        @Json(name = "decimals") val decimals: Int,
        @Json(name = "symbol") val symbol: String,
        @Json(name = "name") val name: String,
        @Json(name = "logoUri") val logoUri: String?
    )
}
