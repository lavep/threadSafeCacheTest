package com.gft.cache.lfu;


import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

public  class FrequencyList<K> {
    private final int frequency;

    private FrequencyList<K> previous;

    private FrequencyList<K> next;

    private final Set<K> keys = new ConcurrentSkipListSet<>();

    public FrequencyList(int frequency) {
        this.frequency = frequency;
    }

    public void addKey(K key) {
        keys.add(key);
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
        if (keys.isEmpty()) {
            return null;
        }
        return keys.iterator().next();
    }

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

    public void setNext(FrequencyList<K> next) {
        this.next = next;
    }

    public int getFrequency() {
        return frequency;
    }

    public FrequencyList<K> getNext() {
        return next;
    }
}