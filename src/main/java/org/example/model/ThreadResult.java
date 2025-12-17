package org.example.model;

public class ThreadResult {
    private String threadName;
    private int countIterations;//кількість разів активації даного потоку

    public ThreadResult(String name, int countIterations) {
        this.threadName = name;
        this.countIterations = countIterations;
    }

    public String getThreadName() {
        return threadName;
    }

    public int getCountIterations() {
        return countIterations;
    }
}
