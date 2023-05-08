package com.autolink.app.downloadmanager;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 下载器
 **/
public final class TaskDispatcher {

    //任务最多并发数
    private int maxTasks = 3;

    //任务线程池
    private ExecutorService executorService;

    private Runnable idleCallback;


    //就绪任务队列
    private final Deque<AsyncTask> readyAsyncTasks = new ArrayDeque<>();
    //正在执行任务队列
    private final Deque<AsyncTask> runningAsyncTasks = new ArrayDeque<>();
    //暂停任务队列
    private final Deque<AsyncTask> pauseAsyncTasks = new ArrayDeque<>();


    TaskDispatcher() {
        executorService = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), new ThreadFactory() {

            AtomicInteger seq = new AtomicInteger();

            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setName("DownloadTask" + seq.getAndIncrement());
                return thread;
            }
        });
    }

    /**
     * 入队
     *
     * @param task 任务
     */
    void enqueue(AsyncTask task) {
        synchronized (this) {
            readyAsyncTasks.add(task);
        }
        promoteAndExecute();
    }

    /**
     * 暂停
     *
     * @param task 任务
     */
    void pause(AsyncTask task) {
        synchronized (this) {
            pauseAsyncTasks.add(task);
        }
    }

    /**
     * 恢复
     *
     * @param task 任务
     */
    void resume(AsyncTask task) {
        synchronized (this) {
            pauseAsyncTasks.remove(task);
            readyAsyncTasks.addFirst(task);
        }
        promoteAndExecute();
    }

    /**
     * 恢复
     *
     * @param task 任务
     */
    void cancel(AsyncTask task) {
        synchronized (this) {
            pauseAsyncTasks.remove(task);
            readyAsyncTasks.remove(task);
            runningAsyncTasks.remove(task);
        }
        promoteAndExecute();
    }

    /**
     * 取消所有任务
     */
    public void cancelAll() {
        for (AsyncTask task : readyAsyncTasks) {
            task.cancel();
        }
        for (AsyncTask task : runningAsyncTasks) {
            task.cancel();
        }
        for (AsyncTask task : pauseAsyncTasks) {
            task.cancel();
        }
    }

    /**
     * 暂停所有任务
     */
    public void pauseAll() {
        for (Task task : readyAsyncTasks) {
            task.pause();
        }
        for (Task task : runningAsyncTasks) {
            task.pause();
        }
    }


    /**
     * 恢复所有任务
     */
    public void resumeAll() {
        for (Task task : pauseAsyncTasks) {
            task.resume();
        }
    }

    /**
     * 推动任务执行
     *
     * @return 是否运行
     */
    private boolean promoteAndExecute() {
        List<AsyncTask> executableTasks = new ArrayList<>();
        boolean isRunning;
        synchronized (this) {
            for (Iterator<AsyncTask> i = readyAsyncTasks.iterator(); i.hasNext(); ) {
                AsyncTask task = i.next();
                if (runningAsyncTasks.size() >= maxTasks) break;
                i.remove();
                executableTasks.add(task);
                runningAsyncTasks.add(task);
            }
            isRunning = runningTasksCount() > 0;
        }
        for (int i = 0, size = executableTasks.size(); i < size; i++) {
            AsyncTask task = executableTasks.get(i);
            task.executeOn(executorService);
        }
        return isRunning;
    }

    public synchronized void setIdleCallback(Runnable idleCallback) {
        this.idleCallback = idleCallback;
    }


    void finished(AsyncTask task) {
        finished(runningAsyncTasks, task);
    }


    private <T> void finished(Deque<T> tasks, T task) {
        Runnable idleCallback;
        synchronized (this) {
            if (!tasks.remove(task)) throw new AssertionError("Call wasn't in-flight!");
            idleCallback = this.idleCallback;
        }
        boolean isRunning = promoteAndExecute();
        if (!isRunning && idleCallback != null) {
            idleCallback.run();
        }
    }

    /**
     * 设置最大任务数
     *
     * @param maxTasks 最大任务数
     */
    public void setMaxTasks(int maxTasks) {
        if (maxTasks < 1) {
            throw new IllegalArgumentException("max < 1: " + maxTasks);
        }
        synchronized (this) {
            this.maxTasks = maxTasks;
        }
        promoteAndExecute();
    }


    public Task getTask(String id) {
        for (Task task : runningAsyncTasks) {
            if (task.request().getId().equals(id)) {
                return task;
            }
        }
        for (Task task : readyAsyncTasks) {
            if (task.request().getId().equals(id)) {
                return task;
            }
        }
        for (Task task : pauseAsyncTasks) {
            if (task.request().getId().equals(id)) {
                return task;
            }
        }
        return null;
    }

    public int getMaxTasks() {
        return maxTasks;
    }

    public synchronized int readyTasksCount() {
        return readyAsyncTasks.size();
    }

    public synchronized int runningTasksCount() {
        return runningAsyncTasks.size();
    }

    public synchronized int pauseTasksCount() {
        return pauseAsyncTasks.size();
    }


}
