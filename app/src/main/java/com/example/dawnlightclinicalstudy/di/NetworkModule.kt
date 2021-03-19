package com.example.dawnlightclinicalstudy.di

import com.example.dawnlightclinicalstudy.data_source.ClientInterceptor
import com.example.dawnlightclinicalstudy.data_source.RetrofitService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Singleton
    @Provides
    @Named("auth_token")
    fun provideAuthToken(): String {
        return "Bearer eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJyVGx3NU5zUWpnX0JiQlhfcU9DOUNXeWpyZWZQNlo2c1Y4Z2xzRmJaMEE0In0.eyJleHAiOjE2MTQxOTM3NTcsImlhdCI6MTYxMzU4ODk1NywianRpIjoiNWZhZmQ4ZjUtYmYxMy00ZmRiLWFiNjUtMDdiMDYwMjI2ZTAyIiwiaXNzIjoiaHR0cDovLzEwLjEwLjMyLjIwMzo5OTExL2F1dGgvcmVhbG1zL2Rhd25saWdodC10ZXN0IiwiYXVkIjoiYWNjb3VudCIsInN1YiI6IjA2MzM0MDhjLWIyYWMtNDU3Mi1hYjBjLWExZjg4OWQ2MmM0MiIsInR5cCI6IkJlYXJlciIsImF6cCI6ImlvdCIsInNlc3Npb25fc3RhdGUiOiJiZWU0ZDMwNi1mZTU5LTQ5YzUtOWE1My1jMjIwYTI4YjIxODkiLCJhY3IiOiIxIiwiYWxsb3dlZC1vcmlnaW5zIjpbIioiXSwicmVhbG1fYWNjZXNzIjp7InJvbGVzIjpbIm9mZmxpbmVfYWNjZXNzIiwidW1hX2F1dGhvcml6YXRpb24iXX0sInJlc291cmNlX2FjY2VzcyI6eyJhY2NvdW50Ijp7InJvbGVzIjpbIm1hbmFnZS1hY2NvdW50IiwibWFuYWdlLWFjY291bnQtbGlua3MiLCJ2aWV3LXByb2ZpbGUiXX0sImlvdCI6eyJyb2xlcyI6WyJURU5BTlRfQURNSU4iXX19LCJzY29wZSI6InByb2ZpbGUgZW1haWwiLCJlbWFpbF92ZXJpZmllZCI6ZmFsc2UsIm5hbWUiOiJJT1QgQWRtaW4iLCJ0ZW5hbnRJZCI6IjFlYTM5MTFlNTkzZmUxMDlhNzEyN2RmYjU1YmZjOTMiLCJtb2JpbGUiOiIxNzc3Nzc3NjAwNSIsInByZWZlcnJlZF91c2VybmFtZSI6IjE3Nzc3Nzc2MDA1IiwiZ2l2ZW5fbmFtZSI6IklPVCBBZG1pbiIsInVzZXJJZCI6IjA2MzM0MDhjLWIyYWMtNDU3Mi1hYjBjLWExZjg4OWQ2MmM0MiJ9.Goc7pE6lg3zKp-BzMrhNT-r9HJjUXr8_NDy05WiJNnieGDqk6-PqZ28f1aeJL2QxaQWoDZaYEFChwiVaMmEAdlJU17ZsV-KixJskSVrkn7nhI24wsXom4XI-YfrL-dx0jtJrryzyo0Y84jLa85KdjlDnCEfjrBd4fznnHO7kkZzd8GP_-kKMC5qSM-RpffBj1BcCQ9Oa5fOu0Wn36cmEO7JFKnl6hW0u-7bEePo9rdYIZ_tRppfEvjEgXY0wNu8-eNCvomB29FBEIOAkkwcrM2QhuTP4dzvQ74Vk-XN0v60yW7RCk1NAs9HIyU9Z7qWbsZl95jY65G8Fapiu93w03Q"
    }

    @Singleton
    @Provides
    @Named("auth_token")
    fun provideInterceptor(): Interceptor {
        return ClientInterceptor().interceptor()
    }

    @Singleton
    @Provides
    fun provideOkHttp(
        interceptor: Interceptor,
    ): OkHttpClient {
        return OkHttpClient.Builder().addInterceptor(interceptor).build()
    }

    @Singleton
    @Provides
    fun provideService(
        client: OkHttpClient,
    ): RetrofitService {
        return Retrofit.Builder()
            .baseUrl("http://34.82.205.71:19099/api/")
            .addConverterFactory(MoshiConverterFactory.create())
            .client(client)
            .build()
            .create(RetrofitService::class.java)
    }
}
