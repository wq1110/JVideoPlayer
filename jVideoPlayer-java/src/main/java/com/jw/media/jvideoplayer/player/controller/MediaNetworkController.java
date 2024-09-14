package com.jw.media.jvideoplayer.player.controller;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.jw.media.jvideoplayer.lib.log.Logger;
import com.jw.media.jvideoplayer.lib.log.LoggerFactory;
import com.jw.media.jvideoplayer.player.listener.Action;
import com.jw.media.jvideoplayer.player.listener.OnConnectionChangeListener;
import com.jw.media.jvideoplayer.player.receiver.MediaNetworkReceiver;

/**
 * Created by Joyce.wang on 2024/3/12 19:33
 *
 * @Description Network change processing center during media playback
 */
public abstract class MediaNetworkController extends MediaGestureController implements OnConnectionChangeListener {
    private static Logger logger = LoggerFactory.getLogger(MediaNetworkReceiver.class.getSimpleName());

    protected ConnectionType currentConnectionType;
    protected MediaNetworkReceiver networkReceiver;
    protected boolean isReceiverRegistered = false;//广播接受者标识

    public MediaNetworkController(@NonNull Context context) {
        super(context);
    }

    public MediaNetworkController(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MediaNetworkController(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MediaNetworkController(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onHandleMessage(Message msg) {
        super.onHandleMessage(msg);
    }

    @Override
    protected void init(Context context) {
        super.init(context);
        initListener();
        registerReceiver();
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private void initListener() {
        networkReceiver = new MediaNetworkReceiver(this);
    }

    @Override
    public void onLocaleChange() {
        // Handle locale changes if needed
    }

    @Override
    public void onConnectionChange(ConnectionType type) {
        logger.i("onConnectionChange currentConnectionType:%s", type.name());

        currentConnectionType = type;
        if (ConnectionType.NONE == type && !mIsCacheFinishFilm) {
            enableProgressSlide(false);
        } else {
            enableProgressSlide(true);
        }
        if (ConnectionType.MOBILE == type && (null != mOnMediaControllerListener && !mOnMediaControllerListener.isMobileTraffic() && isPlaying())) {
            pause();
            mOnMediaControllerListener.onNetworkChange(mActivity, new Action<Boolean>() {
                @Override
                public void call(Boolean b) {
                    if (b) {
                        resumePlay();
                    }
                }
            });
        } else if (null != mOnMediaControllerListener) {
            mOnMediaControllerListener.onNetworkChange(type);
        }
    }

    private void registerReceiver() {
        if (!isReceiverRegistered) {
            isReceiverRegistered = true;
            IntentFilter filter = new IntentFilter();
            filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            filter.addAction(Intent.ACTION_LOCALE_CHANGED);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                mContext.registerReceiver(networkReceiver, filter, Context.RECEIVER_EXPORTED);
            } else {
                mContext.registerReceiver(networkReceiver, filter);
            }
        }
    }

    private void unRegisterReceiver() {
        if (isReceiverRegistered) {
            isReceiverRegistered = false;
            mContext.unregisterReceiver(networkReceiver);
        }
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unRegisterReceiver();
    }
}
