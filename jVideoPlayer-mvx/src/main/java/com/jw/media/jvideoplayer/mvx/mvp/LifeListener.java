package com.jw.media.jvideoplayer.mvx.mvp;

import android.content.Intent;
import android.os.Bundle;

/**
 * Created by Joyce.wang on 2024/9/13 14:32
 *
 * @Description TODO
 */
public interface LifeListener {
    void onActivityResult(int requestCode, int resultCode, Intent data);

    void onViewCreated();

    void onCreate(Bundle savedInstanceState, Bundle initDatas);

    void onResume();

    void onPause();

    void onStart();

    void onStop();

    void onDestroy();

    void onRestore(Bundle savedInstanceState);

    void onSave(Bundle savedInstanceState);
}