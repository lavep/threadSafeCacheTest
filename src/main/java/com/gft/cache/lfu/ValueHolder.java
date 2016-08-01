package com.gft.cache.lfu;


public  class ValueHolder<K, V> {
    private final K key;

    private final V value;

    private final FrequencyList<K> frequencyList;


    public ValueHolder(K key, V value, FrequencyList<K> frequencyList) {
        this.key = key;
        this.value = value;
        this.frequencyList = frequencyList;
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }

    public FrequencyList<K> getFrequencyList() {
        return frequencyList;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (!(o instanceof ValueHolder)) {
            return false;
        }
        if (((ValueHolder) o).getFrequencyList() != frequencyList) {
            return false;
        }

        if (!((ValueHolder) o).getKey().equals(key)) {
            return false;
        }
        return ((ValueHolder) o).getValue().equals(value);
    }

}