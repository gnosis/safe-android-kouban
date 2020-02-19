package io.gnosis.kouban.data.backend

import io.gnosis.kouban.data.backend.dto.ServiceTokenInfo
import io.gnosis.kouban.data.BuildConfig
import retrofit2.http.GET
import retrofit2.http.Path

interface RelayServiceApi {

    @GET("v1/tokens/{address}/")
    suspend fun tokenInfo(@Path("address") address: String): ServiceTokenInfo

    companion object {
        const val BASE_URL = BuildConfig.RELAY_SERVICE_URL
    }
}
