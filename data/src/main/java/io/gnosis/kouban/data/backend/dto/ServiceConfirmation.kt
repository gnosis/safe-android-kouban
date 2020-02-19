package io.gnosis.kouban.data.backend.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ServiceConfirmation(
    @Json(name = "owner") val owner: String,
    @Json(name = "submissionDate") val submissionDate: String,
    @Json(name = "signature") val signature: String?
)
