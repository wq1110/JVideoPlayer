package com.jw.media.jvideoplayer.viewmodel;

import android.app.Application;
import android.content.Intent;

import androidx.annotation.NonNull;
import com.jw.media.jvideoplayer.mvx.viewmodel.BaseViewModel;

/**
 * Created by Joyce.wang on 2024/3/26 15:26
 *
 * @Description TODO
 */
public class SimplePlayViewModel extends BaseViewModel {
    private static final String TAG = SimplePlayViewModel.class.getSimpleName();
    public SimplePlayViewModel(@NonNull Application application) {
        super(application);
    }

    public void initData(Intent intent) {

    }
}
