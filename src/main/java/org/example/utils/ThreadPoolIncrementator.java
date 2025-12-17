package org.example.utils;

public class ThreadPoolIncrementator {
    private int value = 0;
    private int limit = 0;
    private int countIterations = 0;

    public ThreadPoolIncrementator(int limit) {
        this.limit = limit;
    }

    public synchronized boolean checkThenTryIncrement() {
        if (value >= limit) {
            return false;
        } else {
            value++;
            return true;
        }
    }

    public int getValue() {
        return value;
    }

    public int getLimit() {
        return limit;
    }
}
