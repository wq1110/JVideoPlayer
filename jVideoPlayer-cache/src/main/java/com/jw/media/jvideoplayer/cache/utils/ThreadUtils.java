package com.jw.media.jvideoplayer.cache.utils;

import android.os.Handler;
import android.os.Looper;
import android.os.Process;

import com.jw.media.jvideoplayer.cache.VideoInfoParseManager;
import com.jw.media.jvideoplayer.lib.log.Logger;
import com.jw.media.jvideoplayer.lib.log.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by Joyce.wang on 2024/9/11 15:02
 *
 * @Description 线程池管理类
 */
public class ThreadUtils {
    private static Logger logger = LoggerFactory.getLogger(ThreadUtils.class.getName());

    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE = Math.max(2, CPU_COUNT + 1); // 保证至少有2个核心
    private static final int MAXIMUM_POOL_SIZE = Math.min(CPU_COUNT * 2 + 1, 128); // 限制最大线程数避免过多
    private static final int KEEP_ALIVE = 1;
    private static final int QUEUE_SIZE = 1 << Math.max(CPU_COUNT, 1); // 防止CPU核心数为0导致错误

    private static final BlockingQueue<Runnable> sThreadPoolWorkQueue = new LinkedBlockingQueue<>(QUEUE_SIZE);
    private static final ExecutorService sThreadPoolExecutor = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE,
            TimeUnit.SECONDS, sThreadPoolWorkQueue, new WorkerThreadFactory(), new ThreadPoolExecutor.DiscardOldestPolicy());

    private static final Handler sMainHandler = new Handler(Looper.getMainLooper());

    public static void runOnUiThread(final Runnable action) {
        runOnUiThread(action, 0);
    }

    public static void runOnUiThread(Runnable r, int delayTime) {
        if (delayTime >= 0) {
            if (delayTime > 0) {
                sMainHandler.postDelayed(r, delayTime);
            } else if (isRunMainThread()) {
                r.run();
            } else {
                sMainHandler.post(r);
            }
        } else {
            logger.e("Invalid delayTime: " + delayTime);
        }
    }
    private static boolean isRunMainThread() {
        return Looper.getMainLooper() == Looper.myLooper();
    }

    // 提交任务时的异常处理建议
    public static <T> Future<T> submitCallbackTask(Callable<T> task) {
        return executeWithExceptionHandling(sThreadPoolExecutor, task);
    }

    public static Future<?> submitRunnableTask(Runnable task) {
        return executeWithExceptionHandling(sThreadPoolExecutor, task);
    }

    private static <T> Future<T> executeWithExceptionHandling(ExecutorService executor, Callable<T> task) {
        try {
            return executor.submit(task);
        } catch (Exception e) {
            logger.e("Callable, executor submit error: %s", e.getMessage());
            return null;
        }
    }

    private static Future<?> executeWithExceptionHandling(ExecutorService executor, Runnable task) {
        try {
            return executor.submit(task);
        } catch (Exception e) {
            logger.e("Runnable, executor submit error: %s", e.getMessage());
            return null;
        }
    }

    /**
     * 关闭线程池，释放资源。应在应用程序退出或不再需要线程池时调用。
     */
    public static void shutdown() {
        if (!sThreadPoolExecutor.isShutdown()) {
            try {
                sThreadPoolExecutor.shutdown();
                logger.i("ThreadPoolExecutor has been shutdown.");
            } catch (SecurityException se) {
                logger.e("Failed to shutdown ThreadPoolExecutor due to security restriction: %s", se.getMessage());
            }
        }
    }

    private static class WorkerThreadFactory implements ThreadFactory {
        public Thread newThread(Runnable r) {
            return new WorkerThread(r);
        }
    }

    private static class WorkerThread extends Thread {
        public WorkerThread(Runnable r) {
            super(r, "play_worker_pool_thread");
        }
        @Override
        public void run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            long startTime = System.currentTimeMillis();
            try {
                super.run();
            } catch (Exception e) {
                logger.e("thread execution failed.", e);
            } finally {
                long endTime = System.currentTimeMillis();
                logger.i("thread execution time: " + (endTime - startTime));
            }
        }
    }
}
