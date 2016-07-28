package com.gft.cache.lfu;

import com.gft.cache.Cache;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Thread safe LFUCache
 */
public class LFUCache<K, V> implements Cache<K, V> {

    private final int maxSize;

    private final double evictionFactor;

    private final int toDeleteOnCacheFull;

    private final Map<K, V> cacheMap = new HashMap<K, V>();

    private FrequencyList<K> firstFrequency = new FrequencyList<K>(0);

    private final Map<K, FrequencyList<K>> keyToFrequency = new HashMap<K, FrequencyList<K>>();

    private final ExecutorService service = Executors.newSingleThreadExecutor();

    private final ReadWriteLock mapLock = new ReentrantReadWriteLock();

    public LFUCache(int maxSize, double evictionFactor) {
        this.maxSize = maxSize;
        this.evictionFactor = evictionFactor;
        toDeleteOnCacheFull = (int) (maxSize * evictionFactor);
    }

    public void put(final K key, final V value) {

        Future addingToFrequencyTask =
                service.submit(new Runnable() {
                    public void run() {
                        try {

                            if (cacheMap.containsKey(key)) {
                                cacheMap.put(key, value);
                                return;
                            }
                            try {
                                mapLock.writeLock().lock();
                                addToFrequencyListWith0Frequency(key);
                                cacheMap.put(key, value);
                            } finally {
                                mapLock.writeLock().unlock();
                            }

                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                    }
                });
//        while (!addingToFrequencyTask.isDone()) {
//            ;
//        }


    }

    public V get(final K key) {
        service.submit(new Runnable() {
            public void run() {
                increaseFrequencyCounter(key);
            }
        });
        try {
            mapLock.readLock().lock();
            return cacheMap.get(key);
        } finally {
            mapLock.readLock().unlock();
        }
    }

    public int size() {
        return cacheMap.size();
    }


    public void evict(final K key) {
        service.submit(new Runnable() {
            public void run() {
                internalEvict(key);
            }
        });

    }


    private void addToFrequencyListWith0Frequency(K key) {
        if (cacheMap.size() == maxSize) {
            evict();
        }
        keyToFrequency.put(key, firstFrequency);
        firstFrequency.addKey(key);
    }

    private void increaseFrequencyCounter(K key) {
        FrequencyList<K> frequency = keyToFrequency.get(key);
        if (frequency != null) {
            FrequencyList<K> newFrequency = frequency.getFrequencyOneHigher();
            newFrequency.addKey(key);
            frequency.removeKey(key);
            keyToFrequency.put(key, newFrequency);
        }
    }


    private void evict() {

        for (int i = 0; i < toDeleteOnCacheFull; i++) {
            K key = firstFrequency.getKey();
            if (key == null) {
                key = firstFrequency.getNext().getKey();
            }
            internalEvict(key);
        }


    }


    private void internalEvict(K key) {
        FrequencyList<K> frequency = keyToFrequency.get(key);
        if (frequency != null) {
            frequency.removeKey(key);
        } else {
            throw new IllegalStateException("Frequency not found");
        }
        keyToFrequency.remove(key);
        try {
            mapLock.writeLock().lock();
            cacheMap.remove(key);
        } finally {
            mapLock.writeLock().unlock();
        }
    }


    private class FrequencyList<K> {
        private final int frequemcy;

        private FrequencyList<K> previous;

        private FrequencyList<K> next;

        private final Set<K> keys = new HashSet<K>();

        public FrequencyList(int frequency) {
            this.frequemcy = frequency;
        }

        public void addKey(K key) {
            keys.add(key);
        }

        public void removeKey(K key) {
            keys.remove(key);
            if (keys.isEmpty() && frequemcy != 0) {
                previous.setNext(next);
                if (next != null) {
                    next.setPrevious(previous);
                }
            }

        }

        public K getKey() {
            if (keys.isEmpty()) {
                return null;
            }
            return keys.iterator().next();
        }

        public FrequencyList<K> getFrequencyOneHigher() {
            if (next == null || next.getFrequemcy() != frequemcy + 1) {
                FrequencyList<K> oneHigerFrequency = new FrequencyList<K>(frequemcy + 1);
                if (next != null) {
                    next.setPrevious(oneHigerFrequency);
                }
                oneHigerFrequency.setPrevious(this);
                oneHigerFrequency.setNext(next);
                next = oneHigerFrequency;
                return oneHigerFrequency;
            } else {
                return next;
            }
        }

        public void setPrevious(FrequencyList<K> previous) {
            this.previous = previous;
        }

        public void setNext(FrequencyList<K> next) {
            this.next = next;
        }

        public int getFrequemcy() {
            return frequemcy;
        }

        public FrequencyList<K> getNext() {
            return next;
        }
    }
}