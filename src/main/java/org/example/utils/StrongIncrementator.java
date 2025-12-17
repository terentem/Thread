package org.example.utils;

public class StrongIncrementator {
    private int value = 0;
    private int countIncrements = 0;
    private int limit;
    private int t1Job = 0;
    private int t2Job = 0;


    public StrongIncrementator(int limit) {
        this.limit = limit;
    }

    public synchronized boolean checkThenDoIncrement() {
        if (value >= limit) {
            return false;
        } else {
            value++;
            countIncrements++;
            String threadName = Thread.currentThread().getName();
            if (threadName.equals("t1")) {
                t1Job++;
            } else {
                t2Job++;
            }
            return true;
        }
    }

    public synchronized int fulfilIncrement() {
        value++;
        countIncrements++;
        return value;
    }

    public synchronized int getValue() {
        return value;
    }

    public synchronized int getCountIncrements() {
        return countIncrements;
    }

    public int getLimit() {
        return limit;
    }

    public int getT1Job() {
        return t1Job;
    }

    public int getT2Job() {
        return t2Job;
    }
}
