package com.jevzo.limitcloud.library.cache

class Cache<K, V> {

    private val cache: MutableMap<K, V> = mutableMapOf()

    fun put(key: K, value: V) {
        synchronized(cache) {
            cache[key] = value
        }
    }

    operator fun set(key: K, value: V) {
        synchronized(cache) {
            cache[key] = value
        }
    }

    fun update(key: K, value: V) {
        synchronized(cache) {
            cache.remove(key)
            cache[key] = value
        }
    }

    fun remove(key: K) {
        synchronized(cache) {
            cache.remove(key)
        }
    }

    operator fun get(key: K): V? {
        synchronized(cache) {
            return cache[key]
        }
    }

    fun getOrDefault(key: K, value: V?): V? {
        synchronized(cache) {
            return cache[key] ?: value
        }
    }

    fun containsKey(key: K): Boolean {
        synchronized(cache) {
            return cache.containsKey(key)
        }
    }

    fun getAll(): List<V> {
        synchronized(cache) {
            return cache.values.toList()
        }
    }

    fun clear() {
        synchronized(cache) {
            cache.clear()
        }
    }

    fun getCacheValues(): MutableList<V> {
        synchronized(cache) {
            return cache.values.toMutableList()
        }
    }
}