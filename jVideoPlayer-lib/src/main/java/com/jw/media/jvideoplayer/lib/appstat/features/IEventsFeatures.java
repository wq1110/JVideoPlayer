package com.jw.media.jvideoplayer.lib.appstat.features;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;

/**
 * Created by Joyce.wang on 2024/9/11 15:46
 *
 * @Description 各种 按键 触控 等事件 层面
 */
public interface IEventsFeatures {
    Observable<Object> userOperationHappening(int duration, TimeUnit timeUnit);
}
