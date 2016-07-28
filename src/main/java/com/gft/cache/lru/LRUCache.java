package com.gft.cache.lru;

import com.gft.cache.Cache;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Thread safe LRUCache
 */
public class LRUCache <K, V> implements Cache<K, V> {

    private CachedLinkedHashMap<K,V> cachedItems;

    public LRUCache(final int maxSize, float evictionFactor) {
        cachedItems=new CachedLinkedHashMap<K, V>(maxSize,evictionFactor);
    }

    public void put(K key, V value) {
        cachedItems.put(key,value);
    }

    public V get(K key) {

        return cachedItems.get(key);
    }

    public void evict(K key) {
        cachedItems.remove(key);
    }

    public int size() {
        return cachedItems.size();
    }

    private class CachedLinkedHashMap<K,V> extends LinkedHashMap<K,V> {
        private final int maxCacheSize;
        public CachedLinkedHashMap(final int maxSize, float evictionFactor) {
            super(maxSize,evictionFactor,true);
            this.maxCacheSize=maxSize;
        }
        protected boolean removeEldestEntry(Map.Entry eldest) {
            return size() > maxCacheSize;
        }
    }

}
