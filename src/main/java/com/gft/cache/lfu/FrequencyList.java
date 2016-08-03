package com.gft.cache.lfu;


import org.openjdk.jmh.annotations.Benchmark;

import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentSkipListMap;

public class FrequencyList<K> {
    private final int frequency;
    private final ConcurrentSkipListMap<K, Boolean> keys = new ConcurrentSkipListMap<>();
    private FrequencyList<K> previous;
    private FrequencyList<K> next;

    public FrequencyList(int frequency) {
        this.frequency = frequency;
    }

    public void addKey(K key) {
        keys.put(key, Boolean.TRUE);
    }

    public void removeKey(K key) {
        keys.remove(key);
        if (keys.isEmpty() && frequency != 0) {
            previous.setNext(next);
            if (next != null) {
                next.setPrevious(previous);
            }
        }

    }

    public K getKey() {
        try {
            if (keys.isEmpty()) {
                return null;
            }
            return keys.firstKey();
        } catch (NoSuchElementException e) {
            return null;
            // do nothing something had to delete the key between check and return
        }
    }

    @Benchmark
    public FrequencyList<K> getFrequencyOneHigher() {
        if (next == null || next.getFrequency() != frequency + 1) {
            FrequencyList<K> oneHigerFrequency = new FrequencyList<>(frequency + 1);
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

    public int getFrequency() {
        return frequency;
    }

    public FrequencyList<K> getNext() {
        return next;
    }

    public void setNext(FrequencyList<K> next) {
        this.next = next;
    }
}