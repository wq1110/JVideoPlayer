package com.jw.media.jvideoplayer.lib.appstat;

import android.app.Application;

import com.jw.media.jvideoplayer.lib.appstat.features.IActivitiesFeatures;
import com.jw.media.jvideoplayer.lib.appstat.features.IAppFeatures;
import com.jw.media.jvideoplayer.lib.appstat.features.IEventsFeatures;

/**
 * Created by Joyce.wang on 2024/9/11 15:48
 *
 * @Description 应用 运行状态 管理类
 */
public enum AppManager {
    INSTANCE;

    private ActivitiesFeatureImpl mActivitiesImpl;
    private AppFeatureImpl mAppImpl;
    private EventsFeatureImpl mEventsImpl;

    public synchronized void init(Application application) {
        if (mActivitiesImpl == null) mActivitiesImpl = new ActivitiesFeatureImpl(application);
        if (mAppImpl == null)  mAppImpl = new AppFeatureImpl(application);
        if (mEventsImpl == null)  mEventsImpl = new EventsFeatureImpl(application);
    }


    public IActivitiesFeatures getActivities() {
        return mActivitiesImpl;
    }

    public IAppFeatures getApp() {
        return mAppImpl;
    }

    public IEventsFeatures getEvents() {
        return mEventsImpl;
    }
}
