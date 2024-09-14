package com.jw.media.jvideoplayer.lib.log;

/**
 * Created by Joyce.wang on 2024/9/10 17:23
 *
 * @Description TODO
 */
public interface LoggerInterface {
    void log(int priority, String tag, String message, Throwable t);
}
