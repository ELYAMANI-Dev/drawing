package com.stepbystepdrawing.HowToDrawPoppyPlaytime.services

import android.util.Log
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.data.config.AdManagerConfig
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.data.model.RemoteAdConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Fetches and caches the remote ad-manager config from [AdManagerConfig.CONFIG_URL].
 * Also exposes apiKeys (mixpanelToken, oneSignalAppId) read from the same JSON.
 */
object RemoteAdConfigService {
    private const val TAG = "RemoteAdConfig"

    @Volatile
    private var config: RemoteAdConfig? = null

    private const val CACHE_KEY = "remote_ad_config_cache"
    private const val CACHE_VERSION_KEY = "remote_ad_config_version"
    private const val CACHE_TIMESTAMP_KEY = "remote_ad_config_ts"
    private const val CACHE_VALIDITY_MS = 1 * 60 * 1000L

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    fun getConfig(): RemoteAdConfig? = config

    suspend fun load(context: android.content.Context, forceRefresh: Boolean = false): RemoteAdConfig? = withContext(Dispatchers.IO) {
        if (config != null && !forceRefresh) return@withContext config

        if (config == null) loadFromCache(context)
        if (config != null && !forceRefresh) return@withContext config

        if (AdManagerConfig.CONFIG_URL.isBlank()) {
            Log.w(TAG, "AD_CONFIG_URL is empty; set it in local.properties and rebuild")
            return@withContext config ?: loadFromCache(context)
        }

        try {
            val request = Request.Builder().url(AdManagerConfig.CONFIG_URL).get().build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                Log.w(TAG, "Bad status ${response.code} for ${AdManagerConfig.CONFIG_URL}")
                return@withContext config ?: loadFromCacheStale(context)
            }
            val body = response.body?.string() ?: return@withContext config ?: loadFromCacheStale(context)
            val json = JSONObject(body)
            val cfg = RemoteAdConfig.fromJson(json)
            config = cfg
            saveToCache(context, json, cfg.configVersion)
            cfg
        } catch (e: Exception) {
            Log.e(TAG, "Load failed", e)
            config ?: loadFromCacheStale(context)
        }
    }

    private fun saveToCache(context: android.content.Context, json: JSONObject, version: Int) {
        try {
            context.getSharedPreferences("ad_config", android.content.Context.MODE_PRIVATE)
                .edit()
                .putString(CACHE_KEY, json.toString())
                .putInt(CACHE_VERSION_KEY, version)
                .putLong(CACHE_TIMESTAMP_KEY, System.currentTimeMillis())
                .apply()
        } catch (_: Exception) {}
    }

    private fun loadFromCache(context: android.content.Context): RemoteAdConfig? {
        return try {
            val prefs = context.getSharedPreferences("ad_config", android.content.Context.MODE_PRIVATE)
            val raw = prefs.getString(CACHE_KEY, null) ?: return null
            val ts = prefs.getLong(CACHE_TIMESTAMP_KEY, 0L)
            if (System.currentTimeMillis() - ts > CACHE_VALIDITY_MS) return null
            val json = JSONObject(raw)
            RemoteAdConfig.fromJson(json).also { config = it }
        } catch (_: Exception) {
            null
        }
    }

    private fun loadFromCacheStale(context: android.content.Context): RemoteAdConfig? {
        return try {
            val prefs = context.getSharedPreferences("ad_config", android.content.Context.MODE_PRIVATE)
            val raw = prefs.getString(CACHE_KEY, null) ?: return null
            val json = JSONObject(raw)
            RemoteAdConfig.fromJson(json).also { config = it }
        } catch (_: Exception) {
            null
        }
    }

    fun clearCache() {
        config = null
    }
}
