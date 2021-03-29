package com.example.dawnlightclinicalstudy.di

import com.example.dawnlightclinicalstudy.data_source.ClientInterceptor
import com.example.dawnlightclinicalstudy.data_source.RetrofitService
import com.facebook.stetho.okhttp3.StethoInterceptor
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
        return "Bearer eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJyVGx3NU5zUWpnX0JiQlhfcU9DOUNXeWpyZWZQNlo2c1Y4Z2xzRmJaMEE0In0.eyJleHAiOjE2MTc1ODM3MDMsImlhdCI6MTYxNjk3ODkwMywianRpIjoiMWYyN2MwZDAtNTgyMC00ZjdlLWE5M2YtM2UwY2M3NzViMmMzIiwiaXNzIjoiaHR0cDovLzEwLjEwLjMyLjIwMTo5OTExL2F1dGgvcmVhbG1zL2Rhd25saWdodC10ZXN0IiwiYXVkIjoiYWNjb3VudCIsInN1YiI6IjA2MzM0MDhjLWIyYWMtNDU3Mi1hYjBjLWExZjg4OWQ2MmM0MiIsInR5cCI6IkJlYXJlciIsImF6cCI6ImlvdCIsInNlc3Npb25fc3RhdGUiOiJlOGE1MDFiMi1kYTlhLTQ2YjEtOGQwZS03MmExNGRjMmNmZGYiLCJhY3IiOiIxIiwiYWxsb3dlZC1vcmlnaW5zIjpbIioiXSwicmVhbG1fYWNjZXNzIjp7InJvbGVzIjpbIm9mZmxpbmVfYWNjZXNzIiwidW1hX2F1dGhvcml6YXRpb24iXX0sInJlc291cmNlX2FjY2VzcyI6eyJhY2NvdW50Ijp7InJvbGVzIjpbIm1hbmFnZS1hY2NvdW50IiwibWFuYWdlLWFjY291bnQtbGlua3MiLCJ2aWV3LXByb2ZpbGUiXX0sImlvdCI6eyJyb2xlcyI6WyJURU5BTlRfQURNSU4iXX19LCJzY29wZSI6InByb2ZpbGUgZW1haWwiLCJlbWFpbF92ZXJpZmllZCI6ZmFsc2UsIm5hbWUiOiJJT1QgQWRtaW4iLCJ0ZW5hbnRJZCI6IjFlYTM5MTFlNTkzZmUxMDlhNzEyN2RmYjU1YmZjOTMiLCJtb2JpbGUiOiIxNzc3Nzc3NjAwNSIsInByZWZlcnJlZF91c2VybmFtZSI6IjE3Nzc3Nzc2MDA1IiwiZ2l2ZW5fbmFtZSI6IklPVCBBZG1pbiIsInVzZXJJZCI6IjA2MzM0MDhjLWIyYWMtNDU3Mi1hYjBjLWExZjg4OWQ2MmM0MiJ9.cRROnqJwArI2m7tPh7ckUvmQoC9MyeTUhYdxlFDQxUU1S9pWIe9MNG3yUzSINg0qZOTveFGnsEnaaTMn5lEz8MtcHV5atUduKQfipI6eOnFXYrrOM9Px387-dH9VuN9GWzpgm7gX_d0bxGRf3l8LDgLcTuzk2BGXzxzbNI6WUK5LwJm3x59yYrOolgymmS1BY4QW-WDtiHxoDk2Sc-cKIk2HGXxj0_JeLOZ3BwskkcRVPKHGCOdrmolHkC4f5Llaip2_YMERs2oTIXthYPkyvQ9s9FbSUqhZOkipy8zCTr8PkgPhbbE3K8gsn0orkflPjT9wFbuYS6QZEaH9JW3ssw"
    }

    @Singleton
    @Provides
    fun provideInterceptor(
        @Named("auth_token") token: String,
    ): Interceptor {
        return ClientInterceptor().interceptor(token)
    }

    @Singleton
    @Provides
    fun provideOkHttp(
        interceptor: Interceptor,
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .addNetworkInterceptor(StethoInterceptor())
            .build()
    }

    @Singleton
    @Provides
    fun provideService(
        client: OkHttpClient,
    ): RetrofitService {
        return Retrofit.Builder()
            .baseUrl("http://34.82.205.71:19099/")
            .addConverterFactory(MoshiConverterFactory.create())
            //.addCallAdapterFactory(CoroutineCallAdapterFactory())
            .client(client)
            .build()
            .create(RetrofitService::class.java)
    }
}
