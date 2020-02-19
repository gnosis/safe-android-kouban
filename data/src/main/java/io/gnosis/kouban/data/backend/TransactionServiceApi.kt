package io.gnosis.kouban.data.backend

import io.gnosis.kouban.data.backend.dto.PaginatedResult
import io.gnosis.kouban.data.backend.dto.ServiceBalance
import io.gnosis.kouban.data.backend.dto.ServiceTransaction
import io.gnosis.kouban.data.backend.dto.ServiceTransactionRequest
import io.gnosis.kouban.data.BuildConfig
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface TransactionServiceApi {

    @GET("v1/safes/{address}/balances/usd/")
    suspend fun loadBalances(@Path("address") address: String): List<ServiceBalance>

    @GET("v1/safes/{address}/transactions/")
    suspend fun loadTransactions(@Path("address") address: String): PaginatedResult<ServiceTransaction>

    @GET("v1/transactions/{hash}/")
    suspend fun loadTransaction(@Path("hash") hash: String): ServiceTransaction

    @POST("v1/safes/{address}/transactions/")
    suspend fun confirmTransaction(@Path("address") address: String, @Body confirmation: ServiceTransactionRequest)

    companion object {
        const val BASE_URL = BuildConfig.TRANSACTION_SERVICE_URL
    }
}
