package com.example.gismemo

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.example.gismemo.api.UnsplashSizingInterceptor


class GisMemo : Application(), ImageLoaderFactory {
    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .components {
                add(UnsplashSizingInterceptor)
            }
            .build()
    }
}