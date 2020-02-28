package io.gnosis.kouban.data.backend

import android.location.Address
import io.gnosis.kouban.data.models.TransactionsDto
import pm.gnosis.model.Solidity
import retrofit2.http.GET
import retrofit2.http.Path

interface MagicApi {

    @GET("1/safes/{address}/transactions_overview")
    suspend fun getTransactions(@Path("address") address: String): TransactionsDto
}
