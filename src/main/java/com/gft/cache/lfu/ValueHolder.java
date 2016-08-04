package com.gft.cache.lfu;


import java.util.concurrent.atomic.AtomicBoolean;

public class ValueHolder<K, V> {

    private final K key;

    private V value;

    private Frequency frequencyObject;

    private AtomicBoolean deleted = new AtomicBoolean(false);

    private AtomicBoolean ready = new AtomicBoolean(false);

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

    public Frequency increaseFrequency() {

        Frequency nextFrequency = frequencyObject.getNextFrequency();
        if (nextFrequency == null || nextFrequency.getFrequency() != frequencyObject.getFrequency() + 1) {
            nextFrequency = new Frequency<K, V>(frequencyObject.getFrequency() + 1, frequencyObject, frequencyObject.getNextFrequency());

        }
        frequencyObject.remove(this);
        nextFrequency.add(this);
        frequencyObject = nextFrequency;
        return frequencyObject;


    }

    public void setZeroFrequency(Frequency<K, V> frequency) {
        frequencyObject = frequency;
        frequencyObject.add(this);
        ready.set(true);
    }

    public Frequency getFrequencyObject() {
        return frequencyObject;
    }

    public Integer getFrequency() {
        return frequencyObject.getFrequency();
    }


    public void deleted() {
        deleted.set(true);
        frequencyObject = null;
    }

    public boolean getDeleted() {
        return deleted.get();
    }

    public void waitTillActive() {
        while (!ready.get()) {
        }
    }
}