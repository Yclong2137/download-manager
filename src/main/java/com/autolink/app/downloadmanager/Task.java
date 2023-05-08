package com.autolink.app.downloadmanager;

public interface Task {


    Request request();

    /**
     * 入队
     *
     * @param callback
     */
    void enqueue(Callback callback);

    /**
     * 取消
     */
    void cancel();


    boolean isCanceled();

    /**
     * 暂停
     */
    void pause();

    boolean isPaused();

    /**
     * 恢复
     */
    void resume();

    /**
     * 进度
     */
    int progress();

}
