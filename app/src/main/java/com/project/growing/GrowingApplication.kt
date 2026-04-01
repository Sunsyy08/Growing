package com.project.growing

import android.app.Application
import coil.Coil
import coil.ImageLoader
import okhttp3.OkHttpClient

class GrowingApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // ── Coil에 ngrok 헤더 추가 ─────────────────────────
        val imageLoader = ImageLoader.Builder(this)
            .okHttpClient {
                OkHttpClient.Builder()
                    .addInterceptor { chain ->
                        val request = chain.request().newBuilder()
                            .addHeader("ngrok-skip-browser-warning", "true")
                            .build()
                        chain.proceed(request)
                    }
                    .build()
            }
            .build()

        Coil.setImageLoader(imageLoader)
    }
}