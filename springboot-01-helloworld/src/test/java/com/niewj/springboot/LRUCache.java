package com.niewj.springboot;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by niewj on 2020/8/26 8:39
 */
public class LRUCache<K, V> {

    private int cap;

    private LinkedHashMap<K, V> cache = new LinkedHashMap(cap, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry eldest) {
            return cache.size() > cap;
        }
    };

    public LRUCache(int cap) {
        this.cap = cap;
    }

    public void put(K key, V value){
        cache.put(key, value);
    }

    public V get(K key){
        return cache.get(key);
    }

    public void clear(){
        cache.clear();
    }

    public V remove(K key){
        V val = cache.remove(key);
        return val;
    }

    public void print(){
        System.out.println(cache.keySet());
    }

}
