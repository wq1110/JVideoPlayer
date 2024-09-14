package com.jw.media.jvideoplayer.lib.log;

/**
 * Created by Joyce.wang on 2024/9/10 17:24
 *
 * @Description TODO
 */
public class LoggerFactory {
    public static Logger getLogger(Class clazz) {
        return getLogger(clazz.getCanonicalName());
    }

    public static Logger getLogger(String tag) {
        if (Logger.getLogger() == null)
            Logger.setLogger(new DefaultLogger());
        return Logger.newInstance(tag);
    }
}
