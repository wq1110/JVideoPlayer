package com.jw.media.jvideoplayer.lib.appstat.features;

import java.util.List;

import io.reactivex.Observable;

/**
 * Created by Joyce.wang on 2024/9/11 15:44
 *
 * @Description 应用 层面
 */
public interface IAppFeatures {
    long getAppRunningTime();

    AppStat getCurrentAppStat();

    boolean isAppForeground();

    boolean isAppBackground();

    Observable<AppStat> appStatChanged();

    void exitApp();

    String collectionAppStatusSnapShot();

    List<SysStat> getSysStatus();

    enum SysStat {
        LOW_MEMORY,
        IO_BUSY
    }

    enum AppStat {
        Background,
        Foreground
    }

    enum AppMemoryWaring {

    }
}
