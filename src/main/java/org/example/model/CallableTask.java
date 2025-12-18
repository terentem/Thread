package org.example.model;

import org.example.utils.ThreadPoolIncrementator;

import java.util.concurrent.Callable;

public class CallableTask implements Callable<ThreadResult> {
    private ThreadPoolIncrementator incrementator;

    public CallableTask(ThreadPoolIncrementator incrementator) {
        this.incrementator = incrementator;
    }

    @Override
    public ThreadResult call() throws Exception {
        String threadName = Thread.currentThread().getName();
        int countIterations = 0;
        while (incrementator.checkThenTryIncrement()) {
            countIterations++;
        }
        return new ThreadResult(threadName, countIterations);
    }
}
