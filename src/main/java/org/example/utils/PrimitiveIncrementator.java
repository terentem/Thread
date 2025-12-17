package org.example.utils;

public class PrimitiveIncrementator {
    private int value;
    private int countIterations = 0;

    public PrimitiveIncrementator(int startValue) {
        this.value = startValue;
    }

    public int add1ToValue() {
        countIterations++;
        int tmp = value;
        tmp = tmp + 1;
        value = tmp;
        return value;
    }

    public int getValue() {
        return value;
    }

    public int getCountIterations() {
        return countIterations;
    }
}