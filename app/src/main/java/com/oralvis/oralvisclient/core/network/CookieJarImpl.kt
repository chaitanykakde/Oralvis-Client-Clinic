package com.oralvis.oralvisclient.core.network

import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import java.util.concurrent.CopyOnWriteArrayList

/**
 * In-memory cookie jar that persists cookies per [HttpUrl] for the session.
 * OkHttp sends stored cookies on matching requests; backend sets accessToken/refreshToken via Set-Cookie.
 */
class CookieJarImpl : CookieJar {

    private val cookieStore = mutableMapOf<String, MutableList<Cookie>>()
    private val lock = Any()

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        return synchronized(lock) {
            cookieStore.entries
                .flatMap { (_, cookies) -> cookies }
                .filter { it.matches(url) }
                .toList()
        }
    }

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        synchronized(lock) {
            cookies.forEach { cookie ->
                val key = cookieKey(cookie)
                val list = cookieStore.getOrPut(key) { CopyOnWriteArrayList() }
                list.removeAll { it.name == cookie.name }
                if (!cookie.persistent || cookie.expiresAt > System.currentTimeMillis()) {
                    list.add(cookie)
                }
            }
        }
    }

    /** Removes all stored cookies (e.g. on logout). */
    fun clearAll() {
        synchronized(lock) {
            cookieStore.clear()
        }
    }

    private fun cookieKey(cookie: Cookie): String = cookie.domain + cookie.path + cookie.name
}
