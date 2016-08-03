package com.gft.cache.lfu;

import com.gft.cache.Cache;

import java.util.Map;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Thread safe LFUCache
 */
public class LFUCache<K, V> implements Cache<K, V> {

    private final int maxSize;

    private final int toStayAfterEvict;

    private final ConcurrentNavigableMap<K, ValueHolder<K, V>> cacheMap = new ConcurrentSkipListMap<>();

    private final ConcurrentNavigableMap<Integer, ConcurrentNavigableMap<K, ValueHolder<K, V>>> frequencies = new ConcurrentSkipListMap<>();

    private final ReadWriteLock keysSortedLock = new ReentrantReadWriteLock();

    public LFUCache(int maxSize, float evictionFactor) {
        this.maxSize = maxSize;
        this.toStayAfterEvict = (int) (maxSize * evictionFactor);

    }


    public void put(final K key, final V value) {
        if (cacheMap.containsKey(key)) {
            return;
        }

        if (maxSize <= cacheMap.size()) {
            evict();
        }
        try {
            keysSortedLock.writeLock().lock();
            if (!cacheMap.containsKey(key)) {

                ValueHolder holder = new ValueHolder<K, V>(key, value);

                cacheMap.put(key, holder);
                addToFrequency(holder);

            }
        } finally

        {
            keysSortedLock.writeLock().unlock();
        }

        ValueHolder holder = cacheMap.getOrDefault(key, new ValueHolder<K, V>(key, value));
        holder.setValue(value);


    }

    private void addToFrequency(ValueHolder<K, V> holder) {
        ConcurrentNavigableMap<K, ValueHolder<K, V>> map = frequencies.getOrDefault(holder.getFrequency(), new ConcurrentSkipListMap<>());
        map.put(holder.getKey(), holder);
        frequencies.put(holder.getFrequency(), map);
    }

    public V get(final K key) {
        if (!cacheMap.containsKey(key)) {
            return null;
        }

        try {
            keysSortedLock.writeLock().lock();
            if (cacheMap.containsKey(key)) {
                ValueHolder<K, V> valueHolder = cacheMap.get(key);
                moveToNextFrequency(valueHolder);
                return valueHolder.getValue();

            } else {
                return null;
            }
        } finally {
            keysSortedLock.writeLock().unlock();
        }

    }

    private void moveToNextFrequency(ValueHolder<K, V> valueHolder) {
        try {
            keysSortedLock.writeLock().lock();
            int oldFrequency = valueHolder.getFrequency();
            valueHolder.increaseFrequency();
            removeFromFrequency(oldFrequency, valueHolder);
            addToFrequency(valueHolder);
        } finally

        {
            keysSortedLock.writeLock().unlock();
        }

    }

    private void removeFromFrequency(int oldFrequency, ValueHolder<K, V> valueHolder) {
        ConcurrentNavigableMap<K, ValueHolder<K, V>> frequency = frequencies.get(oldFrequency);
        frequency.remove(valueHolder.getKey());
        if (frequency.isEmpty()) {
            frequencies.remove(oldFrequency);
        }
    }

    public int size() {
        return cacheMap.size();
    }


    public void evict(final K key) {
        try {
            keysSortedLock.writeLock().lock();
            ValueHolder<K, V> toRemove = cacheMap.remove(key);
            removeFromFrequency(toRemove.getFrequency(), toRemove);
        } finally {
            keysSortedLock.writeLock().unlock();

        }
    }


    private void evict() {

        try {
            keysSortedLock.writeLock().lock();
            while (cacheMap.size() > toStayAfterEvict) {

                Map.Entry<Integer, ConcurrentNavigableMap<K, ValueHolder<K, V>>> frequencyEntry = frequencies.firstEntry();
                ConcurrentNavigableMap<K, ValueHolder<K, V>> frequency = frequencyEntry.getValue();
                Map.Entry<K, ValueHolder<K, V>> value = frequency.pollFirstEntry();
                if (frequency.isEmpty()) {
                    frequencies.remove(frequencyEntry.getKey());
                }
                cacheMap.remove(value.getKey());


            }
        } finally {
            keysSortedLock.writeLock().unlock();

        }
    }


}