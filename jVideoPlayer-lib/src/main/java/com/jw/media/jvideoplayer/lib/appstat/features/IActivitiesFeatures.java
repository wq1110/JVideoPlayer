package com.jw.media.jvideoplayer.lib.appstat.features;

import android.app.Activity;
import android.content.ComponentName;

import java.util.List;

/**
 * Created by Joyce.wang on 2024/9/11 15:43
 *
 * @Description Activity层面
 */
public interface IActivitiesFeatures {
    Activity getTopActivity();
    List<Activity> getAllActivity();
    void clearAllActivity();
    void addActivitiesEventListener(IActivitiesEventListener listener);
    void removeActivitiesEventListener(IActivitiesEventListener listener);

    interface IActivitiesEventListener {
        void onNewActivityIn(ComponentName newActivity, ComponentName invokeActivity);
    }
}
