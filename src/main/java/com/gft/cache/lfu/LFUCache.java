package com.gft.cache.lfu;

import com.gft.cache.Cache;

import java.util.HashMap;
import java.util.Map;

/**
 * Thread safe LFUCache
 */
public class LFUCache<K, V> implements Cache<K, V> {

    private final int maxSize;

    private final Map<K, ValueHolder<K, V>> cacheMap;

    private final FrequencyList<K, V> frequencyList = new FrequencyList<K, V>();

    public LFUCache(int maxSize) {
        this.maxSize = maxSize;
        cacheMap = new HashMap<>(maxSize);
    }

    public void put(final K key, final V value) {
        if (cacheMap.containsKey(key)) {
            return;
        }

        if (maxSize <= cacheMap.size()) {
            evictLeastFrequentUsed();
        }

        if (!cacheMap.containsKey(key)) {
            ValueHolder holder = new ValueHolder<K, V>(key, value);
            cacheMap.put(key, holder);
            frequencyList.addToFrequencyList(holder);

        }

        ValueHolder holder = cacheMap.getOrDefault(key, new ValueHolder<K, V>(key, value));
        holder.setValue(value);


    }

    public V get(final K key) {
        if (!cacheMap.containsKey(key)) {
            return null;
        }

        ValueHolder<K, V> valueHolder = cacheMap.get(key);
        if (valueHolder != null) {

            valueHolder.waitTillActive();
            frequencyList.moveToNextFrequency(valueHolder);
            return valueHolder.getValue();

        } else {
            return null;
        }

    }

    public int size() {
        return cacheMap.size();
    }


    public void evict(final K key) {
        ValueHolder<K, V> holder = cacheMap.remove(key);
        frequencyList.remove(holder);
    }


    private void evictLeastFrequentUsed() {
        K key = frequencyList.pollLeastUsed().getKey();
        cacheMap.remove(key);
    }
}