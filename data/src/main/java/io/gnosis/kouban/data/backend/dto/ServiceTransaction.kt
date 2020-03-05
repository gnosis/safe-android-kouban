package io.gnosis.kouban.data.backend.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ServiceTransaction(
    @Json(name = "safe") val safe: String?,
    @Json(name = "to") val to: String?,
    @Json(name = "value") val value: String,
    @Json(name = "data") val data: String?,
    @Json(name = "operation") val operation: Int,
    @Json(name = "gasToken") val gasToken: String?,
    @Json(name = "safeTxGas") val safeTxGas: String,
    @Json(name = "baseGas") val baseGas: String,
    @Json(name = "gasPrice") val gasPrice: String,
    @Json(name = "refundReceiver") val refundReceiver: String?,
    @Json(name = "nonce") val nonce: String,
    @Json(name = "safeTxHash") val safeTxHash: String,
    @Json(name = "submissionDate") val submissionDate: String,
    @Json(name = "executionDate") val executionDate: String?,
    @Json(name = "confirmations") val confirmations: List<ServiceConfirmation>,
    @Json(name = "isExecuted") val isExecuted: Boolean,
    @Json(name = "transactionHash") val transactionHash: String?
)
