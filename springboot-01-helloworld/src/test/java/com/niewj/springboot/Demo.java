package com.niewj.springboot;

import java.lang.ref.SoftReference;
import java.util.concurrent.TimeUnit;

public class Demo {

    public static void main(String[] args) {
        LRUCache<Integer, SoftReference<Ref>> cache = new LRUCache(100);
        for (int i = 0; i < 120; i++) {
            cache.put(i, new SoftReference(new Ref()));
            cache.print();
            try {
                TimeUnit.MILLISECONDS.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    static class Ref{
        private byte[] data;
        public Ref(){
            this.data = new byte[1024 * 1024];
        }
    }
}