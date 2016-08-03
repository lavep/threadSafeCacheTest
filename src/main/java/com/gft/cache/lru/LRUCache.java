package com.gft.cache.lru;

import com.gft.cache.Cache;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Thread safe LRUCache
 */
public class LRUCache <K, V> implements Cache<K, V> {

    private CachedLinkedHashMap<K,V> cachedItems;

    public LRUCache(final int maxSize) {
        cachedItems = new CachedLinkedHashMap<K, V>(maxSize);
    }

    public synchronized void put(K key, V value) {
        cachedItems.put(key,value);
    }

    public synchronized V get(K key) {

        return cachedItems.get(key);
    }

    public synchronized void evict(K key) {
        cachedItems.remove(key);
    }

    public int size() {
        return cachedItems.size();
    }

    private class CachedLinkedHashMap<K,V> extends LinkedHashMap<K,V> {
        private final int maxCacheSize;

        public CachedLinkedHashMap(final int maxSize) {
            super(maxSize, 0.75f, true);
            this.maxCacheSize=maxSize;
        }
        protected boolean removeEldestEntry(Map.Entry eldest) {
            return size() > maxCacheSize;
        }
    }

}
