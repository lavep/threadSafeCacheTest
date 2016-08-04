package com.gft.cache.lfu;

import java.util.HashMap;
import java.util.Map;


public class Frequency<K, V> {

    private final int frequency;
    private final Map<K, ValueHolder<K, V>> values = new HashMap<>();
    private Frequency<K, V> nextFrequency;
    private Frequency<K, V> prevFrequency;

    public Frequency(int frequency, Frequency<K, V> prevFrequency, Frequency<K, V> nextFrequency) {
        this.frequency = frequency;
        this.prevFrequency = prevFrequency;

        this.nextFrequency = nextFrequency;

        if (prevFrequency != null) {
            prevFrequency.setNextFrequency(this);
        }
        if (nextFrequency != null) {
            nextFrequency.setPrevFrequency(this);
        }

    }


    public boolean isEmpty() {
        return values.isEmpty();
    }

    public void add(ValueHolder<K, V> valueHolder) {
        values.put(valueHolder.getKey(), valueHolder);
    }

    public void remove(ValueHolder<K, V> valueHolder) {
        values.remove(valueHolder.getKey());

    }

    public Frequency<K, V> getNextFrequency() {
        return nextFrequency;
    }

    public void setNextFrequency(Frequency<K, V> nextFrequency) {
        this.nextFrequency = nextFrequency;
    }

    public int getFrequency() {
        return frequency;
    }

    public Frequency<K, V> getPrevFrequency() {
        return prevFrequency;
    }

    public void setPrevFrequency(Frequency<K, V> prevFrequency) {
        this.prevFrequency = prevFrequency;
    }

    public Frequency<K, V> removeNode() {
        if (nextFrequency != null) {
            nextFrequency.setPrevFrequency(prevFrequency);
        }
        if (prevFrequency != null) {
            prevFrequency.setNextFrequency(nextFrequency);
        }
        return nextFrequency;
    }

    public ValueHolder<K, V> poll() {
        Map.Entry<K, ValueHolder<K, V>> entry = values.entrySet().iterator().next();
        values.remove(entry.getKey());
        return entry.getValue();
    }
}
