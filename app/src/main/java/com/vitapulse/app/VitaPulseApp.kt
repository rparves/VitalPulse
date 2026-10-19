package com.vitapulse.app

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.AnimatedImageDecoder
import com.vitapulse.app.notify.Reminders

class VitaPulseApp : Application(), ImageLoaderFactory {
    /** Coil image loader with native animated-GIF decoding (Android 9+). */
    override fun newImageLoader(): ImageLoader =
        ImageLoader.Builder(this)
            .components { add(AnimatedImageDecoder.Factory()) }
            .crossfade(true)
            .build()

    override fun onCreate() {
        super.onCreate()
        Reminders.createChannel(this)
        Reminders.schedule(this)
    }
}
