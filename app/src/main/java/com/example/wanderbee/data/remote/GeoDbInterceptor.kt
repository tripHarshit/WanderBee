package com.example.wanderbee.data.remote

import com.example.wanderbee.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response

class GeoDbInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .addHeader("X-RapidAPI-Key", BuildConfig.GEO_DB_API_KEY)
            .addHeader("X-RapidAPI-Host", "wft-geo-db.p.rapidapi.com")
            .build()
        return chain.proceed(request)
    }
}
