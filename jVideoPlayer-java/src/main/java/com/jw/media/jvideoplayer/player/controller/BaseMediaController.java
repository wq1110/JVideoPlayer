package com.jw.media.jvideoplayer.player.controller;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.AttributeSet;
import android.view.InflateException;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.jw.media.jvideoplayer.lib.log.Logger;
import com.jw.media.jvideoplayer.lib.log.LoggerFactory;
import com.jw.media.jvideoplayer.player.base.OnPlayerEventListener;
import com.jw.media.jvideoplayer.player.base.PlayerStatus;
import com.jw.media.jvideoplayer.player.base.VideoPlayer;
import com.jw.media.jvideoplayer.player.base.render.ViewUtils;
import com.jw.media.jvideoplayer.player.listener.OnMediaControllerListener;

/**
 * Created by Joyce.wang on 2024/4/2 18:50
 *
 * @Description Base class for media playback controls.
 */
public abstract class BaseMediaController extends FrameLayout implements IMediaController, OnPlayerEventListener {
    private static Logger logger = LoggerFactory.getLogger(BaseMediaController.class.getName());

    protected Context mContext;
    protected View mRoot;
    //依附的容器Activity
    protected Activity mActivity;
    //Activity界面的中布局的查询工具
    protected ViewUtils mViewUtils;
    //屏幕宽度
    protected int mScreenWidth;
    //屏幕高度
    protected int mScreenHeight;
    //记录竖屏BottomBox的高度
    protected int mBottomBoxInitHeight;
    //音频焦点的监听
    protected AudioManager mAudioManager;
    protected int mMaxVolume;
    protected VideoPlayer mVideoPlayer;
    protected boolean mIsSeries = false;//是否是剧集
    protected boolean mIsPortrait = true;//是否是竖屏展示
    protected boolean mIsTrailer;
    protected boolean mIsPrepared = false;
    protected boolean mIsCacheFinishFilm = false;//是否是缓存完成的影片
    protected boolean mIsFreeTrail = false;
    protected OnMediaControllerListener mOnMediaControllerListener;
    protected HandlerThread mBackgroundThread;
    protected Handler mBackgroundHandler;
    public BaseMediaController(@NonNull Context context) {
        super(context);
        init(context);
    }

    public BaseMediaController(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public BaseMediaController(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public BaseMediaController(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }
    public void setVideoPlayer(@NonNull VideoPlayer videoPlayer) {
        this.mVideoPlayer = videoPlayer;
        this.mVideoPlayer.setOnPlayerEventListener(this);
    }
    protected void init(Context context) {
        mRoot = this;
        mContext = context;
        mActivity = (Activity) context;
        mViewUtils = new ViewUtils((Activity) mContext);
        initInflate(mContext);
        mScreenWidth = mContext.getResources().getDisplayMetrics().widthPixels;
        mScreenHeight = mContext.getResources().getDisplayMetrics().heightPixels;
        mAudioManager = (AudioManager) mContext.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        mMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        mBackgroundThread = new HandlerThread("media controller thread");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    protected void initInflate(Context context) {
        try {
            View.inflate(context, getLayoutId(), this);
        } catch (InflateException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mScreenWidth = mContext.getResources().getDisplayMetrics().widthPixels;
        mScreenHeight = mContext.getResources().getDisplayMetrics().heightPixels;
        mIsPortrait = (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT);
    }

    public void setVideoUrl(String url) {
        if (mVideoPlayer != null) {
            mVideoPlayer.setVideoUrl(url);
        }
    }

    public void resumePlay() {
        if (mVideoPlayer != null) {
            mVideoPlayer.resumePlay();
        }
    }

    public void pause() {
        if (mVideoPlayer != null) {
            mVideoPlayer.pause();
        }
    }

    public void seekTo(long position) {
        if (mVideoPlayer != null) {
            mVideoPlayer.seekTo(position);
        }
    }

    public void release() {
        if (mVideoPlayer != null) {
            mVideoPlayer.release();
        }
    }

    public long getCurrentPosition() {
        if (mVideoPlayer != null) {
            return mVideoPlayer.getCurrentPosition();
        }
        return 0;
    }

    public long getDuration() {
        if (mVideoPlayer != null) {
            return mVideoPlayer.getDuration();
        }
        return -1;
    }

    public boolean isInPlaybackState() {
        if (mVideoPlayer != null) {
            return mVideoPlayer.isInPlaybackState();
        }
        return false;
    }

    public boolean isPlaying() {
        if (mVideoPlayer != null) {
            return mVideoPlayer.isPlaying();
        }
        return false;
    }

    public PlayerStatus getCurrentPlayState() {
        if (mVideoPlayer != null) {
            return mVideoPlayer.getCurrentPlayState();
        }
        return PlayerStatus.STATE_IDLE;
    }

    public int getCurrentBufferPercentage() {
        if (mVideoPlayer != null) {
            return mVideoPlayer.getCurrentBufferPercentage();
        }
        return 0;
    }

    @Override
    public void setOnMediaControllerListener(OnMediaControllerListener l) {
        this.mOnMediaControllerListener = l;
    }

    @Override
    public void resetPlayer() {
        mBackgroundHandler.post(this::release);
    }


    protected void onDestroy() {
        if (mBackgroundHandler != null) {
            mBackgroundHandler.removeCallbacksAndMessages(null);
        }
    }

    public void setIsSeries(boolean isSeries) {
        this.mIsSeries = isSeries;
    }

    protected abstract int getLayoutId();
}
