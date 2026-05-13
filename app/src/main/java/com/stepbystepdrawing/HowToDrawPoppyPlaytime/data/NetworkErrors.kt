package com.stepbystepdrawing.HowToDrawPoppyPlaytime.data

import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException

/** Best-effort: distinguish “no pipe” from HTTP/server errors. */
fun Throwable.isLikelyNoNetwork(): Boolean {
    var t: Throwable? = this
    while (t != null) {
        when (t) {
            is UnknownHostException -> return true
            is SocketTimeoutException -> return true
            is ConnectException -> return true
            is SSLException -> return true
            is IOException -> {
                val m = t.message?.lowercase() ?: ""
                if (
                    "network is unreachable" in m ||
                    "failed to connect" in m ||
                    "connection refused" in m ||
                    "connection reset" in m ||
                    "broken pipe" in m
                ) {
                    return true
                }
            }
        }
        t = t.cause
    }
    return false
}
