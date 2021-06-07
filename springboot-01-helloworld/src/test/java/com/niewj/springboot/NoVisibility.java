package com.niewj.springboot;

public class NoVisibility {
    private static boolean ready;
    private static int number;

    private static class ReaderThread extends Thread {
        public void run() {
            while (!ready)
                Thread.yield();
            System.out.println(number);
        }
    }

    public static void main(String[] args) {
        ReaderThread t = new ReaderThread();
        t.setPriority(Thread.MIN_PRIORITY);
        t.start();

        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
        number = 42;
        ready = true;
    }
}