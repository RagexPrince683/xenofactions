package com.hfr.tdm;

import java.util.concurrent.ConcurrentLinkedQueue;

/** Transfers TDM network work to the main server tick thread. */
public final class TDMServerTaskQueue {

    private static final ConcurrentLinkedQueue<Runnable> SERVER_TASKS =
            new ConcurrentLinkedQueue<Runnable>();

    private TDMServerTaskQueue() { }

    public static void schedule(Runnable task) {
        if (task != null) {
            SERVER_TASKS.add(task);
        }
    }

    public static void runScheduledTasks() {
        Runnable task;
        while ((task = SERVER_TASKS.poll()) != null) {
            task.run();
        }
    }

    public static void clear() {
        SERVER_TASKS.clear();
    }
}
