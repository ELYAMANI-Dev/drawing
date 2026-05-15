package com.stepbystepdrawing.HowToDrawPoppyPlaytime.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

object DrawingApi {
    const val PUBLIC_LABUBU_APP_URL =
        "https://elyamani-dev.github.io/ff-skin-tools-api/api/garten-of-banban.json"

    suspend fun fetchPublicAppJson(url: String = PUBLIC_LABUBU_APP_URL): String =
        withContext(Dispatchers.IO) {
            get(url)
        }

    private fun get(url: String): String {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 15_000
        connection.readTimeout = 15_000
        connection.useCaches = false
        connection.setRequestProperty("Cache-Control", "no-cache")
        return try {
            if (connection.responseCode !in 200..299)
                throw IllegalStateException("HTTP ${connection.responseCode}")
            val body = connection.inputStream.bufferedReader().use { it.readText() }.trim()
            if (body.isEmpty()) throw IllegalStateException("Empty response body")
            body
        } finally {
            connection.disconnect()
        }
    }
}
