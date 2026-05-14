package com.stepbystepdrawing.HowToDrawPoppyPlaytime

import android.app.Application
import coil.Coil
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache

/**
 * Application class. Configures Coil with constrained memory/disk cache
 * to prevent OOM on devices with limited RAM.
 */
class DrawingStepsApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Coil.setImageLoader(
            ImageLoader.Builder(this)
                .memoryCache {
                    MemoryCache.Builder(this)
                        .maxSizePercent(0.20) // 20% of heap (default is 25%)
                        .build()
                }
                .diskCache {
                    DiskCache.Builder()
                        .directory(cacheDir.resolve("coil_cache"))
                        .maxSizeBytes(50L * 1024 * 1024) // 50 MB disk cache
                        .build()
                }
                .crossfade(false)
                .build()
        )
    }
}
