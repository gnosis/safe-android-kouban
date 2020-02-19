package io.gnosis.kouban.core.di

import com.squareup.moshi.Moshi
import io.gnosis.kouban.core.BuildConfig
import io.gnosis.kouban.data.backend.adapter.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import timber.log.Timber
import java.util.concurrent.TimeUnit

val networkModule = module {
    single {
        OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor(object : HttpLoggingInterceptor.Logger {
                override fun log(message: String) {
                    Timber.i(message)
                }
            }).apply { level = HttpLoggingInterceptor.Level.BODY })
            .build()
    }

    single {
        Moshi.Builder()
            .add(WeiAdapter())
            .add(HexNumberAdapter())
            .add(DecimalNumberAdapter())
            .add(DefaultNumberAdapter())
            .add(SolidityAddressAdapter())
            .build()
    }

    factory<Retrofit.Builder> {
        Retrofit.Builder()
            .addConverterFactory(MoshiConverterFactory.create(get()))
            .client(get())
    }
}
