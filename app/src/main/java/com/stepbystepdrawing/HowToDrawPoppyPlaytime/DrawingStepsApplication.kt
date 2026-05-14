package com.stepbystepdrawing.HowToDrawPoppyPlaytime

import android.app.Application
import android.graphics.Bitmap
import coil.Coil
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache

class DrawingStepsApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Coil.setImageLoader(
            ImageLoader.Builder(this)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .memoryCache {
                    MemoryCache.Builder(this)
                        .maxSizePercent(0.15)
                        .build()
                }
                .diskCache {
                    DiskCache.Builder()
                        .directory(cacheDir.resolve("coil_cache"))
                        .maxSizeBytes(50L * 1024 * 1024)
                        .build()
                }
                .crossfade(false)
                .build()
        )
    }
}
