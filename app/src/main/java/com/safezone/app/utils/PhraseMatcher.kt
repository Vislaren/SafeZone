package com.safezone.app.utils

/**
 * Flexible text-phrase matcher. Normalises input, splits into tokens, and
 * returns true when the target phrase's token sequence is a subsequence of
 * the heard tokens OR when the Levenshtein ratio is above [threshold].
 *
 * Good enough for "red protocol", "call my brother", "I forgot my wallet" style triggers.
 */
object PhraseMatcher {

    fun matches(heard: String, target: String, threshold: Double = 0.82): Boolean {
        if (heard.isBlank() || target.isBlank()) return false
        val h = normalise(heard)
        val t = normalise(target)
        if (h.contains(t)) return true
        if (containsAsSubsequence(h.split(' '), t.split(' '))) return true
        return similarity(h, t) >= threshold
    }

    private fun normalise(s: String): String =
        s.lowercase().replace(Regex("[^a-z0-9 ]"), " ").replace(Regex("\\s+"), " ").trim()

    private fun containsAsSubsequence(heard: List<String>, target: List<String>): Boolean {
        if (target.isEmpty()) return false
        var i = 0
        for (w in heard) { if (w == target[i]) { i++; if (i == target.size) return true } }
        return false
    }

    private fun similarity(a: String, b: String): Double {
        val d = levenshtein(a, b)
        val max = maxOf(a.length, b.length).coerceAtLeast(1)
        return 1.0 - d.toDouble() / max
    }

    private fun levenshtein(a: String, b: String): Int {
        val m = a.length; val n = b.length
        if (m == 0) return n; if (n == 0) return m
        val prev = IntArray(n + 1) { it }
        val curr = IntArray(n + 1)
        for (i in 1..m) {
            curr[0] = i
            for (j in 1..n) {
                val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                curr[j] = minOf(curr[j - 1] + 1, prev[j] + 1, prev[j - 1] + cost)
            }
            System.arraycopy(curr, 0, prev, 0, n + 1)
        }
        return prev[n]
    }
}
