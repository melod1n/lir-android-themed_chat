package com.android.lir.di

import android.content.Context
import com.android.lir.dataclases.AlwaysString
import com.android.lir.manager.VoxCallManager
import com.android.lir.manager.VoxClientManager
import com.android.lir.network.AuthRepo
import com.android.lir.network.AuthRepoImpl
import com.android.lir.network.LirApi
import com.android.lir.utils.AlwaysStringDeserializer
import com.android.lir.utils.ResultCallFactory
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.voximplant.sdk.Voximplant
import com.voximplant.sdk.client.ClientConfig
import com.voximplant.sdk.client.IClient
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class Modules {
    @Singleton
    @Provides
    fun provideOkHttpClient() = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }).build()

    @Singleton
    @Provides
    fun provideGson() : Gson = GsonBuilder()
        .registerTypeAdapter(AlwaysString::class.java, AlwaysStringDeserializer())
        .setLenient()
        .create()

    @Singleton
    @Provides
    fun provideRetrofit(
        client: OkHttpClient,
        gson : Gson
    ): Retrofit = Retrofit.Builder()
        .baseUrl("http://188.120.251.81/api/")
        .addConverterFactory(GsonConverterFactory.create(gson))
        .addCallAdapterFactory(ResultCallFactory())
        .client(client)
        .build()

    @Singleton
    @Provides
    fun provideService(retrofit: Retrofit) = retrofit.create(LirApi::class.java)

    @Singleton
    @Provides
    fun provideVoximplantClient(@ApplicationContext context: Context): IClient =
        Voximplant.getClientInstance(Executors.newSingleThreadExecutor(), context, ClientConfig())

    @Singleton
    @Provides
    fun provideClientManager(voxyClient: IClient) = VoxClientManager().apply {
        client = voxyClient
    }

    @Singleton
    @Provides
    fun provideCallManager(voxyClient: IClient, @ApplicationContext context: Context) = VoxCallManager(voxyClient, context)

    @Singleton
    @Provides
    fun provideResources(@ApplicationContext context: Context) = context.resources

    @Singleton
    @Provides
    fun provideAppScope() = CoroutineScope(SupervisorJob())
}


@InstallIn(SingletonComponent::class)
@Module
abstract class RepoModule {
    @Singleton
    @Binds
    abstract fun bindAuthRepo(impl: AuthRepoImpl): AuthRepo
}