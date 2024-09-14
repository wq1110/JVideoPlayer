package com.jw.media.jvideoplayer.mvx.mvp;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.StrictMode;

import java.io.File;
import java.io.FilenameFilter;
import java.util.LinkedList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.regex.Pattern;

/**
 * Created by Joyce.wang on 2024/9/13 14:27
 *
 * @Description API实现 基类
 *             目标 让所有API子类 请求,处理流程固定稳定
 *             微任务管理: 可以撤销之前所有Tag相同的请求
 */
public abstract class BaseAPIImpl implements BaseAPI {
    private final LinkedList<APIRequestTask> taskList = new LinkedList<>();

    private static HandlerThread handlerThread;

    private static ExecutorService mExecutor = Executors.newFixedThreadPool(calculateBestThreadCount());


    static {
        handlerThread = new HandlerThread(BaseAPIImpl.class.getSimpleName());
        handlerThread.start();
    }

    private final int OP_TASK_NEW = 0x54;
    private final int OP_TASK_DONE = 0x55;

    private final Handler mHandler = new Handler(handlerThread.getLooper(), new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case OP_TASK_NEW: {
                    APIRequestTask task = (APIRequestTask) msg.obj;
                    requestImpl(task);
                }
                break;
                case OP_TASK_DONE: {
                    APIRequestTask task = (APIRequestTask) msg.obj;
                    if (taskList.contains(task)) {
                        requestDoneImpl(task);
                        task.dispatchResult();
                    }

                }
                break;
            }
            return false;
        }
    });

    protected BaseAPIImpl() {
    }

    /**
     * 请求一个任务
     *
     * @param task
     */
    @SuppressWarnings("JavaDoc")
    protected final void request(APIRequestTask task) {
        if (task.getListener() != null && task.getListener() instanceof InvalidRequestAPIListener)
            return;
        mHandler.obtainMessage(OP_TASK_NEW, task).sendToTarget();
    }

    private void requestDone(APIRequestTask task) {
        mHandler.obtainMessage(OP_TASK_DONE, task).sendToTarget();
    }

    private void requestDoneImpl(APIRequestTask task) {
        taskList.remove(task);
    }

    private void requestImpl(APIRequestTask task) {
        if (task.isClearSameTypeBefore()) {
            clearSameTypeRequestBefore(task.getType());
        }
        taskList.offerLast(task);
        mExecutor.execute(task);
    }

    private void clearSameTypeRequestBefore(String tag) {
        for (int i = 0; i < taskList.size(); i++) {
            APIRequestTask task = taskList.get(i);
            if (task.getType().equals(tag)) {
                taskList.remove(task);
                --i;
            }
        }
    }

    protected final <T> void successOnListener(APIDataListener<T> listener, T result) {
        if (listener != null) {
            if (result != null)
                listener.onSuccess(result);
            else
                listener.onError(new Exception("result is null"));
        }
    }

    protected final void errorOnListener(APIDataListener listener, Throwable e) {
        if (listener != null) listener.onError(e);
    }

    /**
     * 处理结果
     *
     * @param resultBean
     * @param listener
     * @param <T>
     * @return 结果成功与否
     */
    @SuppressWarnings("JavaDoc")
    <T> boolean takeResult(APIResultBean<T> resultBean, APIDataListener<T> listener) {
        if (resultBean.isSuccess()) {
            //成功
            if (listener != null) {
                listener.onSuccess(resultBean.getData());
            }
            return true;
        } else {
            //失败
            if (listener != null) {
                //ServerResultBean不能依赖APIError接口,所以只能如此,正确方式应该是 结果模型 实现 APIError接口,这样就与
                final String errorCode = resultBean.getErrorCode();
                final String errorMsg = resultBean.getErrorMessage();
                listener.onError(new APIError() {
                    @Override
                    public String getErrorCode() {
                        return errorCode;
                    }

                    @Override
                    public String getErrorMessage() {
                        return errorMsg;
                    }
                });
            }
            return false;
        }
    }

    public <T> boolean isResultSuccess(APIResultBean<T> resultBean) {
        return takeResult(resultBean, null);
    }

    private static int calculateBestThreadCount() {
        String CPU_NAME_REGEX = "cpu[0-9]+";
        String CPU_LOCATION = "/sys/devices/system/cpu/";
        int MAXIMUM_AUTOMATIC_THREAD_COUNT = 4;
        // We override the current ThreadPolicy to allow disk reads.
        // This shouldn't actually do disk-IO and accesses a device file.
        StrictMode.ThreadPolicy originalPolicy = StrictMode.allowThreadDiskReads();
        File[] cpus = null;
        try {
            File cpuInfo = new File(CPU_LOCATION);
            final Pattern cpuNamePattern = Pattern.compile(CPU_NAME_REGEX);
            cpus = cpuInfo.listFiles(new FilenameFilter() {
                @SuppressWarnings("unused")
                @Override
                public boolean accept(File file, String s) {
                    return cpuNamePattern.matcher(s).matches();
                }
            });
        } catch (Throwable t) {

        } finally {
            StrictMode.setThreadPolicy(originalPolicy);
        }

        int cpuCount = cpus != null ? cpus.length : 0;
        int availableProcessors = Math.max(1, Runtime.getRuntime().availableProcessors());
        return Math.min(MAXIMUM_AUTOMATIC_THREAD_COUNT, Math.max(availableProcessors, cpuCount));
    }

    public void executeInWorkThread(Runnable runnable) {
        mExecutor.execute(runnable);
    }

    @SuppressWarnings("unchecked")
    protected class APIRequestTask<DataType> extends FutureTask {
        private final boolean isClearSameTypeBefore;
        private final APIDataListener<DataType> mListener;
        private final String type;

        public APIRequestTask(String tag, boolean clearTasksInSameTag, Runnable runnable, APIDataListener<DataType> listener) {
            super(new Callable() {
                @Override
                public Object call() throws Exception {
                    runnable.run();
                    return null;
                }
            });
            mListener = listener;
            type = tag;
            isClearSameTypeBefore = clearTasksInSameTag;
        }

        public APIRequestTask(String tag, boolean clearTasksInSameTag, Callable<APIResultBean<DataType>> callable, APIDataListener<DataType> listener) {
            super(callable);
            mListener = listener;
            type = tag;
            isClearSameTypeBefore = clearTasksInSameTag;
        }

        @SuppressWarnings("unused")
        public APIDataListener<DataType> getListener() {
            return mListener;
        }

        public String getType() {
            return type;
        }

        public boolean isClearSameTypeBefore() {
            return isClearSameTypeBefore;
        }

        @Override
        public APIResultBean<DataType> get() throws InterruptedException, ExecutionException {
            return (APIResultBean<DataType>) super.get();
        }

        @Override
        protected void done() {
            super.done();
            requestDone(this);
        }

        private void dispatchResult() {
            try {
                APIResultBean<DataType> result = get();
                if (result == null) return;
                takeResult(result, mListener);
            } catch (Throwable e) {
                if (mListener != null) mListener.onError(e);
            }
        }
    }
}