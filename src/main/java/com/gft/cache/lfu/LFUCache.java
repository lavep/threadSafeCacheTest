package com.gft.cache.lfu;

import com.gft.cache.Cache;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;
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
    private final ConcurrentSkipListMap<Integer, ConcurrentSkipListMap<K, Boolean>> frequencies = new ConcurrentSkipListMap<>();


    public LFUCache(int maxSize, double evictionFactor) {
        this.maxSize = maxSize;
        this.toStayAfterEvict = (int) (maxSize * evictionFactor);
    }


    public void put(final K key, final V value) {


        while (true) {
            ValueHolder<K, V> oldValue = cacheMap.get(key);

            if (oldValue != null) {
                ValueHolder<K, V> newValue = new ValueHolder<>(key, value, oldValue.getFrequency());
                if (cacheMap.replace(key, oldValue, newValue)) {
                    //if we replaced value nothing should happen
                    return;
                } else {
                    //The key either is deleted or moved to other frequency list
                    continue;
                }

            }

            if (cacheMap.size() >= maxSize) {
                evict();
                continue;
            }

            try {

                cacheMapLock.writeLock().lock();
                ValueHolder<K, V> newValue = new ValueHolder<>(key, value, 0);

                if (cacheMap.putIfAbsent(key, newValue) == null) {

                    if (newValue.equals(cacheMap.get(key))) {
                        addToFrequency(key, 0);
                        return;
                    }


                } else {
                    // The key was added by different thread
                }
            } finally {

                cacheMapLock.writeLock().unlock();
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


        ValueHolder<K, V> holder = cacheMap.get(key);
        if (holder == null) {
            //not found in map must be already removed
            return null;
        }
        try {
            cacheMapLock.writeLock().lock();
            holder = cacheMap.get(key);
            if (holder == null) {
                //not found in map must be already removed
                return null;
            }


            int oldFrequency = holder.getFrequency();
            int newFrequency = oldFrequency + 1;
            ValueHolder<K, V> newHolder = new ValueHolder<>(key, holder.getValue(), holder.getFrequency() + 1);

            cacheMap.replace(key, newHolder);
            addToFrequency(key, newFrequency);
            removeFromFrequency(key, oldFrequency);
            return holder.getValue();
        } finally {
            cacheMapLock.writeLock().unlock();


        }


    }


    private void evict() {
        try {
            int i = 0;

            evictCacheLock.writeLock().lock();

            while (cacheMap.size() > toStayAfterEvict) {
                i++;
                try {
                    cacheMapLock.writeLock().lock();
                    K key = frequencies.firstEntry().getValue().firstKey();

                    if (key != null) {
                        internalEvict(key);
                    } else {
                        System.out.println(i + " " + cacheMap.size() + " cos nie tak " + key);
                    }
                    if (i > 10000) {
                        System.out.println(i + " " + cacheMap.size() + " " + key);
                    }
                } finally {
                    cacheMapLock.writeLock().unlock();
                }

            }
        } finally {
            evictCacheLock.writeLock().unlock();
        }

    }

    private void addToFrequency(K key, int frequency) {
        try {
            cacheMapLock.writeLock().lock();
            if (frequencies.containsKey(frequency)) {
                frequencies.get(frequency).put(key, Boolean.TRUE);
            } else {
                ConcurrentSkipListMap<K, Boolean> currentFrequency = new ConcurrentSkipListMap<>();
                currentFrequency.put(key, Boolean.TRUE);
                frequencies.put(frequency, currentFrequency);
            }
        } finally {
            cacheMapLock.writeLock().unlock();
        }
    }

    private void removeFromFrequency(K key, int frequency) {
        try {
            cacheMapLock.writeLock().lock();
            ConcurrentSkipListMap<K, Boolean> currentFrequency = frequencies.get(frequency);
            currentFrequency.remove(key);
            if (currentFrequency.isEmpty()) {
                frequencies.remove(frequency);
            }
        } finally {
            cacheMapLock.writeLock().unlock();
        }
    }


    private void internalEvict(K key) {
        try {
            cacheMapLock.writeLock().lock();

            if (cacheMap.containsKey(key)) {
                ValueHolder<K, V> value = cacheMap.remove(key);
                removeFromFrequency(key, value.getFrequency());
            } else {
                throw new IllegalStateException("Key not foudn " + key);
            }
        } finally {
            cacheMapLock.writeLock().unlock();
        }

    }
}