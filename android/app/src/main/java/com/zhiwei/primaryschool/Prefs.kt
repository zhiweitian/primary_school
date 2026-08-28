package com.zhiwei.primaryschool

import android.content.Context
import android.content.SharedPreferences
import java.security.MessageDigest
import kotlin.math.max
import kotlin.math.roundToInt

object Prefs {
    const val POINT_COST = 40.0
    const val PLAY_MS = 10 * 60 * 1000L
    const val DEFAULT_URL =
        "https://zhiweitian.github.io/primary_school/knowledge-tree/index.html"

    private const val NAME = "kiosk"
    private lateinit var p: SharedPreferences

    fun init(ctx: Context) {
        p = ctx.applicationContext.getSharedPreferences(NAME, Context.MODE_PRIVATE)
    }

    private fun sp(): SharedPreferences = p

    fun studyUrl(): String = sp().getString("url", DEFAULT_URL) ?: DEFAULT_URL
    fun setStudyUrl(url: String) {
        sp().edit().putString("url", url.trim()).apply()
    }

    fun balance(): Double = Double.fromBits(sp().getLong("bal", 0.0.toBits()))
    fun setBalance(v: Double) {
        val n = (max(0.0, v) * 4).roundToInt() / 4.0
        sp().edit().putLong("bal", n.toBits()).apply()
    }

    fun isPlayActive(): Boolean = sp().getBoolean("play", false) && playRemainingMs() > 0

    fun playRemainingMs(): Long = max(0L, sp().getLong("remain", 0L))

    fun startPlay() {
        sp().edit()
            .putBoolean("play", true)
            .putLong("remain", PLAY_MS)
            .apply()
    }

    fun consumePlay(dtMs: Long) {
        if (!sp().getBoolean("play", false)) return
        val left = max(0L, playRemainingMs() - dtMs)
        val ed = sp().edit().putLong("remain", left)
        if (left <= 0L) ed.putBoolean("play", false)
        ed.apply()
    }

    fun endPlay() {
        sp().edit().putBoolean("play", false).putLong("remain", 0L).apply()
    }

    fun hasPin(): Boolean = !sp().getString("pin", "").isNullOrEmpty()

    fun setPin(pin: String) {
        sp().edit().putString("pin", sha(pin)).apply()
    }

    fun checkPin(pin: String): Boolean = sha(pin) == (sp().getString("pin", "") ?: "")

    private fun sha(s: String): String {
        val d = MessageDigest.getInstance("SHA-256").digest(s.toByteArray())
        return d.joinToString("") { "%02x".format(it) }
    }
}
