package com.akrivos.eos;

import org.apache.log4j.Logger;

import java.util.concurrent.*;

public class ServerThreadPool extends ThreadPoolExecutor implements ThreadPool {
    private static final Logger logger = Logger.getLogger(ServerThreadPool.class);

    private static final int CORE_POOL_SIZE = 16;
    private static final int MAX_POOL_SIZE = 64;
    private static final int KEEP_ALIVE_TIME = 60;
    private static final int QUEUE_SIZE = 10;

    /**
     * Created a ThreadPool with the default constant values of this class.
     */
    public ServerThreadPool() {
        super(CORE_POOL_SIZE, MAX_POOL_SIZE, KEEP_ALIVE_TIME, TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(QUEUE_SIZE));
    }

    /**
     * Creates a new ThreadPool with the given initial parameters.
     *
     * @param corePoolSize    the number of threads to keep in the pool,
     *                        even if they are idle.
     * @param maximumPoolSize the maximum number of threads to allow in the pool.
     * @param keepAliveTime   when the number of threads is greater than the core,
     *                        this is the maximum time that excess idle threads
     *                        will wait for new tasks before terminating.
     * @param unit            the time unit for the keepAliveTime argument.
     * @param workQueue       the queue to use for holding tasks before they are
     *                        executed. This queue will hold only the Runnable
     *                        tasks submitted by the execute method.
     */
    public ServerThreadPool(int corePoolSize, int maximumPoolSize,
                            long keepAliveTime, TimeUnit unit,
                            BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }

    /**
     * @see com.akrivos.eos.ThreadPool#enqueueTask(Runnable)
     */
    @Override
    public boolean enqueueTask(Runnable task) {
        try {
            super.execute(task);
            return true;
        } catch (RejectedExecutionException e) {
            logger.warn("Cannot accept the task for execution", e);
            return false;
        }
    }
}
