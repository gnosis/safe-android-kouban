package io.gnosis.kouban.data.backend.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ServiceTransactionRequest(
    @Json(name = "to") val to: String?,
    @Json(name = "value") val value: String,
    @Json(name = "data") val data: String?,
    @Json(name = "operation") val operation: Int,
    @Json(name = "gasToken") val gasToken: String?,
    @Json(name = "safeTxGas") val safeTxGas: String,
    @Json(name = "baseGas") val baseGas: String,
    @Json(name = "gasPrice") val gasPrice: String,
    @Json(name = "refundReceiver") val refundReceiver: String?,
    @Json(name = "nonce") val nonce: String?,
    @Json(name = "contractTransactionHash") val safeTxHash: String,
    @Json(name = "sender") val sender: String,
    @Json(name = "confirmationType") val confirmationType: String,
    @Json(name = "signature") val signature: String?,
    @Json(name = "transactionHash") val transactionHash: String? = null
) {
    companion object {
        const val CONFIRMATION = "CONFIRMATION"
        const val EXECUTION = "EXECUTION"
    }
}
