package com.example.wanderbee.data.cache

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CulturalTipsMemoryCache @Inject constructor() {
    private val cache = ConcurrentHashMap<String, Triple<String, Long, Long>>()
    private val cacheDuration = TimeUnit.DAYS.toMillis(7)
    private val maxCacheSize = 100

    fun get(countryName: String): String? {
        val cached = cache[countryName] ?: return null
        val (value, timestamp, _) = cached

        return if (System.currentTimeMillis() - timestamp < cacheDuration) {

            cache[countryName] = Triple(value, timestamp, System.currentTimeMillis())
            value
        } else {
            cache.remove(countryName)
            null
        }
    }

    fun put(countryName: String, value: String) {
        if (cache.size >= maxCacheSize) {
            evictOldestEntries()
        }

        val currentTime = System.currentTimeMillis()
        cache[countryName] = Triple(value, currentTime, currentTime)
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