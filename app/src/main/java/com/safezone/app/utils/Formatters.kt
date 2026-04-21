package com.safezone.app.utils

import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object Formatters {

    private val dateFmt = DateTimeFormatter.ofPattern("MMM dd, yyyy").withZone(ZoneId.systemDefault())
    private val timeFmt = DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault())

    fun formatDate(isoTs: String?): String = isoTs?.let {
        runCatching { dateFmt.format(Instant.parse(it)) }.getOrDefault("—")
    } ?: "—"

    fun formatTime(isoTs: String?): String = isoTs?.let {
        runCatching { timeFmt.format(Instant.parse(it)) }.getOrDefault("—")
    } ?: "—"

    fun formatDurationShort(seconds: Long): String {
        val d = Duration.ofSeconds(seconds.coerceAtLeast(0))
        val m = d.toMinutes()
        val s = d.seconds % 60
        return if (m > 0) "${m}m" else "${s}s"
    }

    fun formatTimer(seconds: Long): String {
        val m = (seconds / 60).toString().padStart(2, '0')
        val s = (seconds % 60).toString().padStart(2, '0')
        return "$m:$s"
    }

    fun formatDistance(meters: Double): String = when {
        meters < 1000 -> "${meters.toInt()}m"
        else -> String.format("%.1f km", meters / 1000.0)
    }

    fun formatMiles(meters: Double): String = String.format("%.1f", meters / 1609.344)
}

/** Haversine distance in meters. */
fun haversineMeters(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
    val R = 6_371_000.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLng = Math.toRadians(lng2 - lng1)
    val a = sin(dLat / 2).let { it * it } +
        cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLng / 2).let { it * it }
    return 2 * R * atan2(sqrt(a), sqrt(1 - a))
}
