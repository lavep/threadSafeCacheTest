package com.gft.cache;

/**
 * Created by e-papz on 7/26/2016.
 */
public interface Cache<K,V> {

    void put(K key,V value);

    V get(K key);

    void evict(K key);

    public int size();
}