package com.gft.cache.lfu;

import com.gft.cache.Cache;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Thread safe LFUCache
 */
public class LFUCache<K, V> implements Cache<K, V> {

    private final int maxSize;


    private final Map<K, ValueHolder<K, V>> cacheMap;

    private final FrequencyList<K, V> frequencyList = new FrequencyList<K, V>();

    private final ReadWriteLock keysSortedLock = new ReentrantReadWriteLock();


    public LFUCache(int maxSize) {
        this.maxSize = maxSize;
        cacheMap = new HashMap<>(maxSize);
    }


    public void put(final K key, final V value) {
        if (cacheMap.containsKey(key)) {
            return;
        }

        try {
            //       keysSortedLock.writeLock().lock();
        if (maxSize <= cacheMap.size()) {
            evictLeastFrequentUsed();
        }

            if (!cacheMap.containsKey(key)) {
                ValueHolder holder = new ValueHolder<K, V>(key, value);
                cacheMap.put(key, holder);
                //addToFrequencyList(holder);
                frequencyList.addToFrequencyList(holder);

            }
        } finally

        {
            //       keysSortedLock.writeLock().unlock();
        }

        ValueHolder holder = cacheMap.getOrDefault(key, new ValueHolder<K, V>(key, value));
        holder.setValue(value);


    }

    public V get(final K key) {
        if (!cacheMap.containsKey(key)) {
            return null;
        }

        try {
            //     keysSortedLock.writeLock().lock();
            if (cacheMap.containsKey(key)) {
                ValueHolder<K, V> valueHolder = cacheMap.get(key);
                valueHolder.waitTillActive();
                frequencyList.moveToNextFrequency(valueHolder);
                return valueHolder.getValue();

            } else {
                return null;
            }
        } finally {
            //    keysSortedLock.writeLock().unlock();
        }

    }

    public int size() {
        return cacheMap.size();
    }


    public void evict(final K key) {
        //  Not supported yet
    }


    private void evictLeastFrequentUsed() {
        K key = frequencyList.pollLeastUsed().getKey();
        cacheMap.remove(key);
    }


}