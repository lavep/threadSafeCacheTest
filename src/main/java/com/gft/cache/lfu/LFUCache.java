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

    //private final NavigableMap<Integer, Map<K, ValueHolder<K, V>>> frequencies = new TreeMap<>();

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
            keysSortedLock.writeLock().lock();
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
            keysSortedLock.writeLock().unlock();
        }

        ValueHolder holder = cacheMap.getOrDefault(key, new ValueHolder<K, V>(key, value));
        holder.setValue(value);


    }

//    private void addToFrequencyList(ValueHolder<K, V> holder) {
//
//        if (frequencies.containsKey(holder.getFrequency())) {
//            frequencies.get(holder.getFrequency()).put(holder.getKey(), holder);
//        } else {
//            Map<K, ValueHolder<K, V>> map = new HashMap<>();
//            map.put(holder.getKey(), holder);
//            frequencies.put(holder.getFrequency(), map);
//        }
//    }

    public V get(final K key) {
        if (!cacheMap.containsKey(key)) {
            return null;
        }

        try {
            keysSortedLock.writeLock().lock();
            if (cacheMap.containsKey(key)) {
                ValueHolder<K, V> valueHolder = cacheMap.get(key);
                frequencyList.moveToNextFrequency(valueHolder);
                //  moveToNextFrequency(valueHolder);
                return valueHolder.getValue();

            } else {
                return null;
            }
        } finally {
            keysSortedLock.writeLock().unlock();
        }

    }

//    private void moveToNextFrequency(ValueHolder<K, V> valueHolder) {
//        try {
//            keysSortedLock.writeLock().lock();
//            int oldFrequency = valueHolder.getFrequency();
//            valueHolder.increaseFrequency();
//            removeFromFrequency(oldFrequency, valueHolder);
//            addToFrequencyList(valueHolder);
//        } finally
//
//        {
//            keysSortedLock.writeLock().unlock();
//        }
//
//    }

//    private void removeFromFrequency(int oldFrequency, ValueHolder<K, V> valueHolder) {
//        Map<K, ValueHolder<K, V>> frequency = frequencies.get(oldFrequency);
//        frequency.remove(valueHolder.getKey());
//        if (frequency.isEmpty()) {
//            frequencies.remove(oldFrequency);
//        }
//    }

    public int size() {
        return cacheMap.size();
    }


    public void evict(final K key) {
        try {
            keysSortedLock.writeLock().lock();
            ValueHolder<K, V> toRemove = cacheMap.remove(key);
            //   removeFromFrequency(toRemove.getFrequency(), toRemove);
        } finally {
            keysSortedLock.writeLock().unlock();

        }
    }


    private void evictLeastFrequentUsed() {
//        Map.Entry<Integer, Map<K, ValueHolder<K, V>>> frequencyEntry = frequencies.firstEntry();
//        Map<K, ValueHolder<K, V>> frequency = frequencyEntry.getValue();
//
//        K key = frequency.entrySet().iterator().next().getKey();
//        frequency.remove(key);
        K key = frequencyList.pollLeastUsed().getKey();
        cacheMap.remove(key);
    }


}