package com.akrivos.eos;

/**
 * A simple ThreadPool interface.
 */
public interface ThreadPool {
    /**
     * Enqueues a task in the ThreadPool.
     *
     * @param task the task to be executed.
     * @return true if the task was executed successfully
     *         without any errors, false otherwise.
     * @throws Exception any exception that might occur.
     */
    boolean enqueueTask(Runnable task) throws Exception;
}