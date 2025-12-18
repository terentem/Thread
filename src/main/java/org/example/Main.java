package org.example;

import org.example.model.CallableTask;
import org.example.model.ThreadResult;
import org.example.utils.PrimitiveIncrementator;
import org.example.utils.StrongIncrementator;
import org.example.utils.ThreadPoolIncrementator;
import org.example.utils.WeakIncrementator;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        String incrementedResult1 = incrementValueWithNoTreads(100);
        System.out.println("Результат виклику incrementValueWithNoThreads: " + incrementedResult1);
        System.out.println("******************************************************");

        String incrementedResult2 = incrementValueAddTreads(1000);
        System.out.println("Результат виклику примітивного incrementValueAddThreads: " + incrementedResult2);
        System.out.println("******************************************************");

        String incrementedResult3 = incrementWithSynchronizedThread(1000);
        System.out.println("Результат виклику  incrementWithSynchronizedThread: " + incrementedResult3);
        System.out.println("******************************************************");

        String incrementedResult4 = incrementWithStrongIncrementator(1000);
        System.out.println("Результат виклику incrementWithStrongIncrementator: " + incrementedResult4);
        System.out.println("******************************************************");

        String incrementedResult5 = incrementViaThreadPool(10000000);
        System.out.println("Результат виклику incrementViaThreadPool: " + incrementedResult5);
        System.out.println("******************************************************");

        String incrementedResult6 = incrementViaThreadPoolExecutor(10000000);
        System.out.println("Результат виклику incrementViaThreadPoolExecutor: " + incrementedResult6);
        System.out.println("******************************************************");
    }

    public static String incrementValueWithNoTreads(int limit) {
        PrimitiveIncrementator incrementator = new PrimitiveIncrementator(0);
        int incrementedValue = 0;
        int i = 0;
        while (incrementator.getValue() < limit) {
            incrementedValue = incrementator.add1ToValue();
            i++;
        }
        return "Значення value досягнуло " + limit + " за " + i + " неатомарних ітерацій (int value++) без потоків.";
    }

    public static String incrementValueAddTreads(int limit) throws InterruptedException {
        PrimitiveIncrementator incrementator = new PrimitiveIncrementator(0);
        Runnable incrementation = new Runnable() {
            @Override
            public void run() {
                int i = 0;
                while (incrementator.getValue() < limit) {
                    incrementator.add1ToValue();
                }
            }
        };
        Thread t1 = new Thread(incrementation);
        Thread t2 = new Thread(incrementation);
        t1.start();
        t2.start();
        t1.join();
        t2.join();

        return "Значення value досягло " + incrementator.getValue() + " двома потоками за " + incrementator.getCountIterations() + " ітерацій додавання +1.";
    }

    public static String incrementWithSynchronizedThread(int limit) throws InterruptedException {
        WeakIncrementator incrementator = new WeakIncrementator();
        Runnable task = new Runnable() {
            @Override
            public void run() {
                while (incrementator.getValue() < limit) {
                    incrementator.add1ToValue();
                }
            }
        };
        Thread t1 = new Thread(task);
        Thread t2 = new Thread(task);
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        return "Значення value досягло " + incrementator.getValue() + " двома синхронізованими потоками за " + incrementator.getCountIncrements() + " інкрементів.";
    }

    public static String incrementWithStrongIncrementator(int limit) throws InterruptedException {
        StrongIncrementator strongIncrementator = new StrongIncrementator(limit);

        Runnable task = new Runnable() {
            @Override
            public void run() {
                while (strongIncrementator.checkThenDoIncrement()) {
                }
            }
        };
        Thread t1 = new Thread(task, "t1");
        Thread t2 = new Thread(task, "t2");
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        System.out.println("******************************************************");
        System.out.println("Monitor obj:");
        //System.out.println(ClassLayout.parseInstance(strongIncrementator).toPrintable());
        return "Значення value досягло " + strongIncrementator.getValue() + " двома синхронізованими (StrongIncrementator) потоками за " + strongIncrementator.getCountIncrements() + " ітерацій додавання +1." + "t1Job= " + strongIncrementator.getT1Job() + ", t2Job= " + strongIncrementator.getT2Job();
    }

    public static String incrementViaThreadPool(int limit) throws InterruptedException, ExecutionException {
        ThreadPoolIncrementator incrementator = new ThreadPoolIncrementator(limit);

        //Оголошуємо частину коду, яка виконується потоками. Використовуємо Callable(щоб отримати результат виконання роботи потоком)
        Callable<ThreadResult> task = new Callable<ThreadResult>() {
            @Override
            public ThreadResult call() throws Exception {
                String threadName = Thread.currentThread().getName();
                int countINcrements = 0;//для підрахунку кількості ітерацій додавання для отримання value=limit
                while (incrementator.checkThenTryIncrement()) {
                    countINcrements++;
                }
                return new ThreadResult(threadName, countINcrements);
            }
        };

        //Підготовчі кроки перед створенням потоків
        ExecutorService pool = Executors.newFixedThreadPool(
                10,
                new ThreadFactory() {
                    @Override
                    public Thread newThread(Runnable r) {
                        return new Thread(r, "thread-" + UUID.randomUUID());
                    }
                }
        );

        //Стартуємо роботу потоків та записуємо результати в futures
        List<Future<ThreadResult>> futures = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Future taskResults = pool.submit(task);
            futures.add(taskResults);
        }

        pool.shutdown();
        pool.awaitTermination(1, TimeUnit.MINUTES);
        int totalWork = 0;
        for (Future<ThreadResult> future : futures) {
            ThreadResult result = future.get();
            totalWork += result.getCountIterations();
            System.out.println(
                    result.getThreadName() + " зробив " + result.getCountIterations() + " інкрементів"
            );
        }
        System.out.println("******************************************************");
        return "Значення value =" + incrementator.getValue() + " досягнуто синхронізованими потоками ThreadPool за " + totalWork + " ітерацій.";
    }

    public static String incrementViaThreadPoolExecutor(int limit) throws InterruptedException, ExecutionException {
        ThreadPoolIncrementator incrementator = new ThreadPoolIncrementator(limit);
        CallableTask task = new CallableTask(incrementator);
        ThreadPoolExecutor executor = new ThreadPoolExecutor(10, 10, 30, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(5),
                new ThreadFactory() {
                    private AtomicInteger counter = new AtomicInteger(1);

                    @Override
                    public Thread newThread(Runnable r) {
                        return new Thread(
                                r,
                                "thread" + counter.getAndIncrement()
                        );
                    }
                },
                new ThreadPoolExecutor.AbortPolicy()
        );

        //Запуск потоків
        List<Future<ThreadResult>> futures = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Future executedTask = executor.submit(task);
            futures.add(executedTask);
        }

        //Зупиняємо потоки
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);

        //Результати роботи кожного потоку
        System.out.println("******************************************************");
        System.out.println("Деталі роботи потоків за методом ThreadPoolExecutor");
        int totalIncrements = 0;
        for (Future<ThreadResult> f : futures) {
            ThreadResult result = f.get();
            totalIncrements += result.getCountIterations();
            System.out.println(result.getThreadName() + " виконав " + result.getCountIterations() + " ітерацій інкременту.");
        }
        System.out.println("******************************************************");
        return "Значення value = " + incrementator.getValue() + " досяглось за " + totalIncrements + " ітерацій інкрементування.";
    }
}

