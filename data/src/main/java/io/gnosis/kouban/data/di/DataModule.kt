package io.gnosis.kouban.data.di

import androidx.room.Room
import io.gnosis.kouban.data.BuildConfig
import io.gnosis.kouban.data.backend.JsonRpcApi
import io.gnosis.kouban.data.backend.RelayServiceApi
import io.gnosis.kouban.data.backend.TransactionServiceApi
import io.gnosis.kouban.data.db.TokensDatabase
import io.gnosis.kouban.data.repositories.SafeRepository
import io.gnosis.kouban.data.repositories.TokenRepository
import okhttp3.OkHttpClient
import org.koin.dsl.module
import pm.gnosis.mnemonic.Bip39
import pm.gnosis.mnemonic.Bip39Generator
import pm.gnosis.mnemonic.android.AndroidWordListProvider
import pm.gnosis.svalinn.common.PreferencesManager
import pm.gnosis.svalinn.security.FingerprintHelper
import pm.gnosis.svalinn.security.impls.AndroidFingerprintHelper
import retrofit2.Retrofit


val dataModule = module {

    single<RelayServiceApi> {
        get<Retrofit.Builder>()
            .baseUrl(RelayServiceApi.BASE_URL)
            .build()
            .create(RelayServiceApi::class.java)
    }

    single<TransactionServiceApi> {
        get<Retrofit.Builder>()
            .baseUrl(TransactionServiceApi.BASE_URL)
            .build()
            .create(TransactionServiceApi::class.java)
    }

    single<JsonRpcApi> {
        val client = get<OkHttpClient>()
            .newBuilder()
            .addInterceptor {
                val request = it.request()
                val builder = request.url.newBuilder()
                val url = builder.addPathSegment(BuildConfig.INFURA_API_KEY).build()
                it.proceed(request.newBuilder().url(url).build())
            }.build()
        get<Retrofit.Builder>()
            .client(client)
            .baseUrl(JsonRpcApi.BASE_URL)
            .build()
            .create(JsonRpcApi::class.java)
    }

    single { Room.databaseBuilder(get(), TokensDatabase::class.java, TokensDatabase.DB_NAME).build() }

    single { get<TokensDatabase>().tokenInfoDao() }

    single<Bip39> { Bip39Generator(AndroidWordListProvider(get())) }

    single { PreferencesManager(get()) }

    single<FingerprintHelper> { AndroidFingerprintHelper(get()) }

    single {
        SafeRepository(get(), get(), get(), get(), get(), get())
    }

    single {
        TokenRepository(get(), get())
    }
}
