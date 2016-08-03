package com.gft.cache.lfu;

import com.gft.cache.Cache;

import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Thread safe LFUCache
 */
public class LFUCache<K, V> implements Cache<K, V> {

    private final int maxSize;

    private final int toStayAfterEvict;

    private final ConcurrentNavigableMap<K, ValueHolder<K, V>> cacheMap;

    private final PriorityBlockingQueue<ValueHolder<K, V>> keysSortedByFrequencies;

    private final ReadWriteLock keysSortedLock = new ReentrantReadWriteLock();

    public LFUCache(int maxSize, float evictionFactor) {
        this.maxSize = maxSize;
        this.toStayAfterEvict = (int) (maxSize * evictionFactor);
        keysSortedByFrequencies = new PriorityBlockingQueue<>(maxSize);
        cacheMap = new ConcurrentSkipListMap<>();
    }


    public void put(final K key, final V value) {


        if (maxSize <= cacheMap.size()) {
            evict();
        }
        try {
            keysSortedLock.writeLock().lock();
            if (!cacheMap.containsKey(key)) {

                ValueHolder holder = new ValueHolder<K, V>(key, value);

                cacheMap.put(key, holder);
                keysSortedByFrequencies.put(holder);

            }
        } finally

        {
            keysSortedLock.writeLock().unlock();
        }

        ValueHolder holder = cacheMap.getOrDefault(key, new ValueHolder<K, V>(key, value));
        holder.setValue(value);


    }


    public V get(final K key) {
        try {
            keysSortedLock.writeLock().lock();
            if (cacheMap.containsKey(key)) {
                ValueHolder<K, V> valueHolder = cacheMap.get(key);
                valueHolder.increaseFrequency();


                keysSortedByFrequencies.remove(valueHolder);
                keysSortedByFrequencies.put(valueHolder);
                return valueHolder.getValue();

            } else {
                return null;
            }
        } finally

        {
            keysSortedLock.writeLock().unlock();
        }

    }

    public int size() {
        return cacheMap.size();
    }


    public void evict(final K key) {
        try {
            keysSortedLock.writeLock().lock();
            ValueHolder<K, V> toRemove = cacheMap.remove(key);
            keysSortedByFrequencies.remove(toRemove);
        } finally {
            keysSortedLock.writeLock().unlock();

        }
    }


    private void evict() {

        try {
            keysSortedLock.writeLock().lock();
//            for (ValueHolder<K, V> val :
//                    keysSortedByFrequencies) {
//                System.out.println(val.getFrequency() + " " + val.getKey());
//            }
            if (keysSortedByFrequencies.size() != cacheMap.size()) {
                throw new IllegalStateException(keysSortedByFrequencies.size() + " - " + cacheMap.size());
            }
            while (cacheMap.size() > toStayAfterEvict) {

                ValueHolder<K, V> poll = keysSortedByFrequencies.poll();


                cacheMap.remove(poll.getKey());


            }
        } finally {
            keysSortedLock.writeLock().unlock();

        }
    }


}