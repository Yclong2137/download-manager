package com.autolink.app.downloadmanager;

/**
 * 下载器
 **/
public class DownloadManager {


    private TaskDispatcher mTaskDispatcher = new TaskDispatcher();

    private DownloadManager() {

    }

    /**
     * 创建任务
     *
     * @param request 下载请求
     * @return 任务
     */
    public Task newTask(Request request) {
        return AsyncTask.newRealTask(this, request);
    }


    public TaskDispatcher dispatcher() {
        return mTaskDispatcher;
    }

    private static final class Holder {
        private static final DownloadManager INSTANCE = new DownloadManager();
    }

    public static DownloadManager getInstance() {
        return Holder.INSTANCE;
    }


}
