package com.ravikiran.nsequant.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/** Read-only client for the desktop app's phone API (GET + bearer key). */
object Api {
    private val http = OkHttpClient.Builder()
        .connectTimeout(6, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)      // an option chain may be computed on demand
        .build()

    class ApiError(message: String) : Exception(message)

    suspend fun get(ctx: Context, path: String): JSONObject = withContext(Dispatchers.IO) {
        if (!Prefs.configured(ctx)) throw ApiError("Set the server address and key in Settings")
        val req = Request.Builder()
            .url(Prefs.server(ctx) + path)
            .header("Authorization", "Bearer " + Prefs.key(ctx))
            .build()
        try {
            http.newCall(req).execute().use { r ->
                val body = r.body?.string() ?: ""
                if (r.code == 401) throw ApiError("Wrong API key — copy it again from the Mac app")
                if (!r.isSuccessful) throw ApiError("Server error ${r.code}: ${body.take(120)}")
                JSONObject(body)
            }
        } catch (e: ApiError) {
            throw e
        } catch (e: java.io.IOException) {
            throw ApiError("Can't reach your Mac (${e.javaClass.simpleName}). Same Wi-Fi / Tailscale? App open?")
        }
    }
}

// ---------------------------------------------------------------- JSON helpers
fun JSONObject.d(k: String): Double? = if (!has(k) || isNull(k)) null else optDouble(k).takeIf { !it.isNaN() }
fun JSONObject.s(k: String): String? = if (!has(k) || isNull(k)) null else optString(k)
fun JSONObject.b(k: String): Boolean = optBoolean(k, false)
fun JSONObject.obj(k: String): JSONObject? = if (!has(k) || isNull(k)) null else optJSONObject(k)
fun JSONObject.arr(k: String): JSONArray = optJSONArray(k) ?: JSONArray()
fun JSONArray.objects(): List<JSONObject> = (0 until length()).mapNotNull { optJSONObject(it) }
fun JSONArray.strings(): List<String> = (0 until length()).map { optString(it) }
fun JSONArray.doubles(): List<Double> = (0 until length()).mapNotNull { optDouble(it).takeIf { v -> !v.isNaN() } }
