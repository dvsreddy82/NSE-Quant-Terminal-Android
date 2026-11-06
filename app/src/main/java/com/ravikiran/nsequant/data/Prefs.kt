package com.ravikiran.nsequant.data

import android.content.Context

/** Server address, API key and monitor state, kept in private app storage. */
object Prefs {
    private const val FILE = "nsequant"

    private fun sp(ctx: Context) = ctx.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    fun server(ctx: Context): String = sp(ctx).getString("server", "") ?: ""
    fun key(ctx: Context): String = sp(ctx).getString("key", "") ?: ""
    fun configured(ctx: Context) = server(ctx).isNotBlank() && key(ctx).isNotBlank()

    fun save(ctx: Context, server: String, key: String) {
        val s = server.trim().trimEnd('/').let { if (it.startsWith("http")) it else "http://$it" }
        sp(ctx).edit().putString("server", s).putString("key", key.trim()).apply()
    }

    fun monitorOn(ctx: Context) = sp(ctx).getBoolean("monitor", false)
    fun setMonitor(ctx: Context, on: Boolean) = sp(ctx).edit().putBoolean("monitor", on).apply()

    /** Alert ids already notified, so each alert pings the phone once. */
    fun seen(ctx: Context): MutableSet<String> = (sp(ctx).getStringSet("seen", emptySet()) ?: emptySet()).toMutableSet()
    fun setSeen(ctx: Context, ids: Set<String>) =
        sp(ctx).edit().putStringSet("seen", ids.toList().takeLast(300).toSet()).apply()
}
