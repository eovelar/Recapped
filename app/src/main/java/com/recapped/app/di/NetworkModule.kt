package com.recapped.app.di

import com.recapped.app.data.remote.DeezerApi
import com.recapped.app.data.remote.LastFmApi
import com.recapped.app.data.remote.SpotifyAccountsApi
import com.recapped.app.data.remote.SpotifyApi
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideMoshi(): Moshi =
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

    @Provides
    @Singleton
    fun provideOkHttp(): OkHttpClient {
        val logger = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        return OkHttpClient.Builder()
            .addInterceptor(logger)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        client: OkHttpClient,
        moshi: Moshi
    ): Retrofit =
        Retrofit.Builder()
            .baseUrl(LastFmApi.BASE_URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

    @Provides
    @Singleton
    fun provideLastFmApi(
        retrofit: Retrofit
    ): LastFmApi =
        retrofit.create(LastFmApi::class.java)

    @Provides
    @Singleton
    fun provideDeezerApi(
        client: OkHttpClient,
        moshi: Moshi
    ): DeezerApi =
        Retrofit.Builder()
            .baseUrl("https://api.deezer.com/")
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(DeezerApi::class.java)

    @Provides
    @Singleton
    fun provideSpotifyApi(
        client: OkHttpClient,
        moshi: Moshi
    ): SpotifyApi =
        Retrofit.Builder()
            .baseUrl("https://api.spotify.com/v1/")
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(SpotifyApi::class.java)

    @Provides
    @Singleton
    fun provideSpotifyAccountsApi(
        client: OkHttpClient,
        moshi: Moshi
    ): SpotifyAccountsApi =
        Retrofit.Builder()
            .baseUrl("https://accounts.spotify.com/")
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(SpotifyAccountsApi::class.java)
}