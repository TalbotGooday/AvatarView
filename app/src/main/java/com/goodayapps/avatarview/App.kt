package com.goodayapps.avatarview

import android.app.Application
import android.os.Build
import coil.Coil
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.util.DebugLogger

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        iniCoil()
    }

    private fun iniCoil() {
        val imageLoader = ImageLoader.Builder(this)
            .components {
                if (Build.VERSION.SDK_INT >= 28) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .logger(DebugLogger())
            .build()

        Coil.setImageLoader(imageLoader)
    }
}