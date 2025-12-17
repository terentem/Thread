package org.example.utils;

public class WeakIncrementator {
    private int value=0;
    private int countIncrements = 0;

    public WeakIncrementator() {
        this.value = value;
    }

    public synchronized int add1ToValue() {
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
}
