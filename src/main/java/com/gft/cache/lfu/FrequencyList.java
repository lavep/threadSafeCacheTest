package com.gft.cache.lfu;


public class FrequencyList<K, V> {


    private Frequency<K, V> firstFrequency = null;

    public synchronized void moveToNextFrequency(ValueHolder<K, V> valueHolder) {
        if (valueHolder.getDeleted()) {
            return;
        }
        Frequency<K, V> oldFrequency = valueHolder.getFrequencyObject();
        Frequency nextFrequencyCount = valueHolder.increaseFrequency();
        if (oldFrequency.isEmpty()) {
            oldFrequency.removeNode();
            if (oldFrequency == firstFrequency) {
                firstFrequency = nextFrequencyCount;
            }
        }


    }

    public synchronized void addToFrequencyList(ValueHolder<K, V> valueHolder) {
        if (valueHolder.getFrequencyObject() != null) {

            throw new IllegalStateException("Adding to Frequency but " + valueHolder.getFrequency());
        }

        if (firstFrequency == null || firstFrequency.getFrequency() != 0) {

            firstFrequency = new Frequency<>(0, null, firstFrequency);

        }

        valueHolder.setZeroFrequency(firstFrequency);

    }


    public synchronized ValueHolder<K, V> pollLeastUsed() {

        ValueHolder<K, V> value = firstFrequency.poll();

        if (firstFrequency.isEmpty()) {

            firstFrequency = firstFrequency.removeNode();

        }
        return value;

    }

    public synchronized void remove(ValueHolder valueHolder) {
        if (valueHolder.getDeleted()) {
            return;
        }
        Frequency<K, V> frequency = valueHolder.getFrequencyObject();
        frequency.remove(valueHolder);

        if (frequency.isEmpty()) {
            Frequency next = frequency.getNextFrequency();
            frequency.removeNode();
            if (frequency == firstFrequency) {
                firstFrequency = next;
            }
        }


    }


}
