package com.jw.media.jvideoplayer.lib.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;

/**
 * Created by Joyce.wang on 2024/9/10 17:27
 *
 * @Description TODO
 */
public class AppContextUtils {
    @SuppressLint("StaticFieldLeak")
    private static Context mContext;

    private AppContextUtils() {
        throw new UnsupportedOperationException("You can't instantiate me...");
    }

    /**
     * 初始化工具类
     *
     * @param context 上下文
     */
    public static void init(Context context) {
        AppContextUtils.mContext = context.getApplicationContext();
    }

    /**
     * 获取ApplicationContext
     *
     * @return ApplicationContext
     */
    public static Context getContext() {
        if (mContext != null) return mContext;
        throw new NullPointerException("You must init first");
    }

    private static String launcherAppId = "";

    public static String getLauncherAppId() {
        return launcherAppId;
    }

    public static void setLauncherAppId(String launcherApp) {
        if(!TextUtils.isEmpty(launcherApp)) {
            launcherAppId = launcherApp;
        }
    }
}
