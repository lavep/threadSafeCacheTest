package com.gft.cache.lfu;

import com.gft.cache.Cache;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Thread safe LFUCache
 */

public class LFUCache<K, V> implements Cache<K, V> {

    private final int maxSize;

    private final int toStayAfterEvict;

    private final ConcurrentMap<K, ValueHolder<K, V>> cacheMap = new ConcurrentHashMap<>();
    private final ReadWriteLock cacheMapLock = new ReentrantReadWriteLock();
    private final ReadWriteLock evictCacheLock = new ReentrantReadWriteLock();
    private FrequencyList<K> firstFrequency = new FrequencyList<>(0);

    public LFUCache(int maxSize, double evictionFactor) {
        this.maxSize = maxSize;
        this.toStayAfterEvict = (int) (maxSize * evictionFactor);
    }

    public void put(final K key, final V value) {

        while (true) {
            ValueHolder<K, V> oldValue = cacheMap.get(key);

            if (oldValue != null) {
                ValueHolder<K, V> newValue = new ValueHolder<>(key, value, oldValue.getFrequencyList());
                if (cacheMap.replace(key, oldValue, newValue)) {
                    //if we replaced value nothing should happen
                    return;
                } else {
                    //The key either is deleted or moved to other frequency list
                    continue;
                }

            }
            if (cacheMap.size() == maxSize) {
                evict();
                continue;
            }


            ValueHolder<K, V> newValue = new ValueHolder<>(key, value, firstFrequency);
            ValueHolder<K, V> valueInCache = cacheMap.putIfAbsent(key, newValue);
            if (valueInCache == null) {
                try {
                    cacheMapLock.writeLock().lock();
                    if (newValue.equals(cacheMap.get(key))) {
                        firstFrequency.addKey(key);
                        return;
                    }
                } finally {
                    cacheMapLock.writeLock().unlock();
                }

            } else {
                // The key was added by different thread
            }


        }


    }

    public V get(final K key) {
        return increaseFrequencyCounterAndGet(key);

    }

    public int size() {
        return cacheMap.size();
    }


    public void evict(final K key) {
        internalEvict(key);


    }


    private V increaseFrequencyCounterAndGet(K key) {

        while (true) {
            ValueHolder<K, V> holder = cacheMap.get(key);
            if (holder == null) {
                //not found in map must be already removed
                return null;
            }
            try {
                cacheMapLock.writeLock().lock();


                FrequencyList<K> frequency = holder.getFrequencyList();

                FrequencyList<K> newFrequency = frequency.getFrequencyOneHigher();
                ValueHolder<K, V> newHolder = new ValueHolder<>(key, holder.getValue(), newFrequency);

                if (cacheMap.replace(key, holder, newHolder)) {

                    newFrequency.addKey(key);
                    frequency.removeKey(key);
                    return holder.getValue();
                }

            } finally {
                cacheMapLock.writeLock().unlock();
            }
        }
    }


    private void evict() {
        try {
            evictCacheLock.writeLock().lock();
            while (cacheMap.size() > toStayAfterEvict) {
                K key = firstFrequency.getKey();
                if (key == null) {
                    key = firstFrequency.getNext().getKey();
                }
                internalEvict(key);
            }
        } finally {
            evictCacheLock.writeLock().unlock();
        }

    }


    private void internalEvict(K key) {
        try {
            cacheMapLock.writeLock().lock();

            if (cacheMap.containsKey(key)) {
                ValueHolder<K, V> value = cacheMap.remove(key);
                value.getFrequencyList().removeKey(key);
            }
        } finally {
            cacheMapLock.writeLock().unlock();
        }

    }


}