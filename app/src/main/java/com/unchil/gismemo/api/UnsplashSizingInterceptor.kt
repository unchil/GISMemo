package com.unchil.gismemo.api

import coil.intercept.Interceptor
import coil.request.ImageResult
import okhttp3.HttpUrl.Companion.toHttpUrl



object UnsplashSizingInterceptor : Interceptor {

    private val testDataUrl = "https://images.unsplash.com/photo-"

    override suspend fun intercept(chain: Interceptor.Chain): ImageResult {
        val data = chain.request.data
        val size = chain.size

        return if (data is String) {

            if(data.startsWith(testDataUrl)){
                val url = data.toHttpUrl()
                    .newBuilder()
                    .addQueryParameter("w", size.width.toString())
                    .addQueryParameter("h", size.height.toString())
                    .build()
                val request = chain.request.newBuilder().data(url).build()
                return chain.proceed(request)
            }

            val url = data.toHttpUrl().newBuilder().build()
            val request = chain.request.newBuilder().data(url).build()
            chain.proceed(request)
        } else {
            chain.proceed(chain.request)
        }

    }
}