package com.autolink.app.downloadmanager;

import java.io.InterruptedIOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;

/**
 * 下载任务
 **/
public class AsyncTask extends NamedRunnable implements Task {


    private boolean executed;

    private DownloadManager mDownloadManager;

    private Callback mCallback;

    private Request mRequest;


    //状态保存
    private final SaveState mState = new SaveState();


    public static AsyncTask newRealTask(DownloadManager downloadManager, Request request) {
        return new AsyncTask(downloadManager, request);
    }

    private AsyncTask(DownloadManager downloadManager, Request request) {
        super("Task-AutoLink-App-Download");
        mDownloadManager = downloadManager;
        this.mRequest = request;
        Logger.debug("current task is " + this);
    }


    @Override
    public Request request() {
        return mRequest;
    }

    @Override
    public void enqueue(Callback callback) {
        synchronized (this) {
            if (executed) throw new IllegalStateException("Already Executed");
            executed = true;
        }
        this.mCallback = callback;
        mDownloadManager.dispatcher().enqueue(this);
    }

    @Override
    public void cancel() {
        if (!mState.canceled) {
            mState.canceled = true;
            mDownloadManager.dispatcher().cancel(this);
            if (mCallback != null)
                mCallback.onFailure(this, new IllegalStateException("Task already canceled"));
        }

    }

    @Override
    public boolean isCanceled() {
        return mState.canceled;
    }

    @Override
    public void pause() {
        if (!mState.paused) {
            mState.paused = true;
            mDownloadManager.dispatcher().pause(this);
        }
    }

    @Override
    public boolean isPaused() {
        return mState.paused;
    }

    @Override
    public void resume() {
        if (mState.paused) {
            mState.paused = false;
            mDownloadManager.dispatcher().resume(this);
        }
    }

    @Override
    public int progress() {
        return mState.progress;
    }


    @Override
    void execute() {
        try {
            Logger.debug("task start execute ...");
            if (mCallback != null) mCallback.onStart(this);
            boolean finished = false;
            Logger.debug("task state=" + mState);
            for (int i = mState.lastIndex; i <= 100; i++) {
                if (mState.canceled) break;

                if (mState.paused) {
                    Logger.debug("Task is paused");
                    break;
                }
                Thread.sleep(100);
                mState.progress = i * 1;
                Logger.debug("task progress=" + mState.progress);
                if (mCallback != null) mCallback.onProgressChanged(this);
                if (i == 100) {
                    finished = true;
                }
                mState.lastIndex = i;
            }
            if (mState.canceled) {
                if (mCallback != null)
                    mCallback.onFailure(this, new IllegalStateException("Task already canceled"));
                return;
            }
            if (finished) {
                if (mCallback != null) mCallback.onSuccess(this);
                Logger.debug("task execute finished");
            }

        } catch (Exception e) {
            if (mCallback != null) mCallback.onFailure(this, e);
        } finally {
            mDownloadManager.dispatcher().finished(this);

        }
    }


    void executeOn(ExecutorService executorService) {
        boolean success = false;
        try {
            executorService.execute(this);
            success = true;
        } catch (RejectedExecutionException e) {
            InterruptedIOException ioException = new InterruptedIOException("executor rejected");
            ioException.initCause(e);
            if (mCallback != null) mCallback.onFailure(this, ioException);
        } finally {
            if (!success) {
                mDownloadManager.dispatcher().finished(this);
            }
        }
    }

    /**
     * 状态保存
     */
    static class SaveState {

        private int lastIndex;
        //进度
        private int progress;

        //取消
        private boolean canceled;

        //暂停
        private boolean paused;

        @Override
        public String toString() {
            return "SaveState{" +
                    "lastIndex=" + lastIndex +
                    ", progress=" + progress +
                    ", canceled=" + canceled +
                    ", paused=" + paused +
                    '}';
        }
    }

}
