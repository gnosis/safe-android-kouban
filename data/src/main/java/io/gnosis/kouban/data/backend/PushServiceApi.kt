package io.gnosis.kouban.data.backend

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import io.gnosis.kouban.data.BuildConfig
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface PushServiceApi {
    @POST("1/observers")
    suspend fun registerDevice(@Body deviceRegistration: DeviceRegistration)

    @POST("{network}/1/safes/{safe_address}/observe")
    suspend fun registerPushes(
        @Path("network") network: String,
        @Path("safe_address") safeChecksumAddress: String,
        @Body pushesRegistration: PushesRegistration
    )

    @JsonClass(generateAdapter = true)
    data class DeviceRegistration(
        @Json(name = "client_id")
        val clientId: String,
        @Json(name = "push_token")
        val pushToken: String
    )

    @JsonClass(generateAdapter = true)
    data class PushesRegistration(
        @Json(name = "client_id")
        val clientId: String
    )

    companion object {

        const val BASE_URL = BuildConfig.PUSH_SERVICE_URL
    }
}


