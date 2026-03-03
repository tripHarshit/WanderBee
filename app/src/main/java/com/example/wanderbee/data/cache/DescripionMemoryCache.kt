package com.example.wanderbee.data.cache

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DescriptionMemoryCache @Inject constructor() {
    private val cache = ConcurrentHashMap<String, Triple<String, Long, Long>>()
    private val cacheDuration = TimeUnit.DAYS.toMillis(7)
    private val maxCacheSize = 100

    fun get(cityName: String): String? {
        val cached = cache[cityName] ?: return null
        val (value, timestamp, _) = cached

        return if (System.currentTimeMillis() - timestamp < cacheDuration) {
            val currentTime = System.currentTimeMillis()
            cache[cityName] = Triple(value, timestamp, currentTime)
            value
        } else {
            cache.remove(cityName)
            null
        }
    }

    fun put(cityName: String, value: String) {
        // Check if we need to evict items
        if (cache.size >= maxCacheSize) {
            evictOldestEntries()
        }

        // Add new item with current timestamp for both creation and last access
        val currentTime = System.currentTimeMillis()
        cache[cityName] = Triple(value, currentTime, currentTime)
    }

    private fun evictOldestEntries() {
        val entriesToRemove = (maxCacheSize * 0.2).toInt().coerceAtLeast(1)

        cache.entries
            .sortedBy { it.value.third }
            .take(entriesToRemove)
            .forEach { cache.remove(it.key) }
    }

    fun clear() {
        cache.clear()
    }

    fun size(): Int {
        return cache.size
    }
}
