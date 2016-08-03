package com.gft.cache.lfu;


public class ValueHolder<K, V> implements Comparable<ValueHolder<K, V>> {
    private final K key;

    private final V value;

    private final int frequency;

    public ValueHolder(K key, V value, int frequency) {
        this.key = key;
        this.value = value;
        this.frequency = frequency;
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }

    public Integer getFrequency() {
        return frequency;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (!(o instanceof ValueHolder)) {
            return false;
        }
        if (((ValueHolder) o).getFrequency() != frequency) {
            return false;
        }

        if (!((ValueHolder) o).getKey().equals(key)) {
            return false;
        }
        return ((ValueHolder) o).getValue().equals(value);
    }


    @Override
    public int compareTo(ValueHolder<K, V> o) {
        return o.getFrequency().compareTo(o.getFrequency());
    }
}