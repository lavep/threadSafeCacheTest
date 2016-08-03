package com.gft.cache.lfu;


import java.util.concurrent.atomic.AtomicInteger;

public class ValueHolder<K, V> implements Comparable<ValueHolder<K, V>> {

    private final K key;
    private final AtomicInteger frequency = new AtomicInteger(0);
    private V value;

    public ValueHolder(K key, V value) {

        this.value = value;
        this.key = key;
    }


    public V getValue() {
        return value;
    }

    public void setValue(V value) {
        this.value = value;
    }

    public K getKey() {
        return key;
    }

    public int increaseFrequency() {
        return frequency.incrementAndGet();
    }

    public Integer getFrequency() {
        return frequency.get();
    }

    @Override
    public int compareTo(ValueHolder<K, V> o) {
        return getFrequency().compareTo(o.getFrequency());

    }


}