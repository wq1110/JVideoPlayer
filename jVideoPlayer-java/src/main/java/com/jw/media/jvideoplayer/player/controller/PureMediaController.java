package com.jw.media.jvideoplayer.player.controller;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Message;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.jw.media.jvideoplayer.java.R;
import com.jw.media.jvideoplayer.lib.log.Logger;
import com.jw.media.jvideoplayer.lib.log.LoggerFactory;
import com.jw.media.jvideoplayer.lib.utils.ToastUtils;
import com.jw.media.jvideoplayer.player.play.PlayConstant;
import com.jw.media.jvideoplayer.player.play.PlayerHelper;
import com.jw.media.jvideoplayer.player.base.PlayerStatus;
import com.jw.media.jvideoplayer.player.base.VideoType;
import com.jw.media.jvideoplayer.player.listener.Action;
import com.jw.media.jvideoplayer.player.listener.OnConnectionChangeListener;

import tv.danmaku.ijk.media.player.IMediaPlayer;

/**
 * Created by Joyce.wang on 2024/8/21 13:57
 *
 * Description: Default playback control view, implementing UI show/hide operations,
 * playback button operations, etc. (playback control view used by mobile devices)
 */
public class PureMediaController extends MediaSubController implements SeekBar.OnSeekBarChangeListener {
    private static Logger logger = LoggerFactory.getLogger(PureMediaController.class.getSimpleName());

    public static final long PLAY_NEXT_EPISODE_THRESHOLD_VALUE = 60 * 1000;
    public static final String KEY_LOADING_EXTRA_INFO = "key_loading_extra_info";
    public static final String KEY_MEDIA_TITLE_INFO = "key_media_title_info";

    protected View mMediaControllerBox;

    //top box
    protected View mMediaTopBox;
    protected ImageView mMediaBack;
    protected TextView mMediaTitle;
    protected View mMediaTopRight;
    protected TextView mMediaFreeTrial;

    //right box
    protected View mMediaRightBox;
    protected ImageView mMediaZoom;

    //center box
    protected LinearLayout mMediaCenterBox;
    protected ImageView mMediaFastRewind10;
    protected ImageView mMediaPlay;//中间播放按钮
    protected ImageView mMediaFastForward10;

    //loading box
    protected View mMediaLoadingBox;
    protected ProgressBar mMediaLoadingBar;
    protected TextView mMediaLoadingPercent;
    protected TextView mMediaLoadingSpeed;
    protected TextView mMediaLoadingExtraInfo;

    //bottom box
    protected LinearLayout mMediaBottomControllerBox;
    protected TextView mMediaNextEpisode;
    protected LinearLayout mMediaBottomBox;
    protected ImageView mMediaBottomPlay;//底部控制栏播放按钮
    protected TextView mMediaCurrentTime;
    protected SeekBar mMediaSeekBar;
    protected TextView mMediaEndTime;
    protected LinearLayout mMediaExtraBox;
    protected LinearLayout mMediaResource;
    protected LinearLayout mMediaEpisode;

    protected long delayTimeToHideControlbox = 5 * 1000;
    protected boolean isDragging;//是否在拖动进度条中，默认为停止拖动，true为在拖动中，false为停止拖动
    protected boolean hasSeeking = false;
    protected long seekCompleteTime;
    protected boolean isBuffering = false;//是否在顿卡中
    protected boolean hasFirstPrepareBuffering = false;
    protected boolean isBufferingBySeek = false;
    protected long prepareTime;
    protected long seekFirstBuffingTime;
    protected long prepareFirstBufferingTime;
    protected long startBufferingPosition = -1;
    protected long bufferingStartTime;
    protected long bufferingEndTime;
    protected long bufferingTime;
    protected long actualTime = 0;//实际播放的时间
    protected long lastPlayTime = -1;
    protected long subActualTime = 0;//字幕实际使用时间
    protected long lastSubPlayTime = -1;
    protected boolean isStatisticSub = false;//字幕使用时间统计开关
    protected boolean isFromCachePage = false;//是否来自缓存页面的播放
    protected boolean isAllowDisplayNextEpisode = true;//是否允许展示next episode

    public PureMediaController(@NonNull Context context) {
        super(context);
    }

    public PureMediaController(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PureMediaController(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public PureMediaController(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.sdk_vod_c_play_layout_default_media_controller;
    }

    @Override
    protected void init(Context context) {
        super.init(context);
        mMediaControllerBox = findViewById(R.id.media_controller_box);

        //top box
        mMediaTopBox = findViewById(R.id.media_top_box);
        mMediaBack = findViewById(R.id.media_back);
        mMediaTitle = findViewById(R.id.media_title);
        mMediaTopRight = findViewById(R.id.media_top_right);
        mMediaFreeTrial = findViewById(R.id.media_free_trial);

        //right box
        mMediaRightBox = findViewById(R.id.media_right_box);
        mMediaZoom = findViewById(R.id.media_zoom);

        //center box
        mMediaCenterBox = findViewById(R.id.media_center_box);
        mMediaFastRewind10 = findViewById(R.id.media_fast_rewind_10);
        mMediaPlay = findViewById(R.id.media_play);
        mMediaFastForward10 = findViewById(R.id.media_fast_forward_10);

        //loading box
        mMediaLoadingBox = findViewById(R.id.media_loading_box);
        mMediaLoadingBar = findViewById(R.id.media_loading_bar);
        mMediaLoadingPercent = findViewById(R.id.media_loading_percent);
        mMediaLoadingSpeed = findViewById(R.id.media_loading_speed);
        mMediaLoadingExtraInfo = findViewById(R.id.media_loading_extra_info);

        //bottom box
        mMediaBottomControllerBox = findViewById(R.id.media_bottom_controller_box);
        mMediaNextEpisode = findViewById(R.id.media_next_episode);
        mMediaBottomBox = findViewById(R.id.media_bottom_box);
        mMediaBottomPlay = findViewById(R.id.media_bottom_play);
        mMediaCurrentTime = findViewById(R.id.media_currentTime);
        mMediaSeekBar = findViewById(R.id.media_seekBar);
        mMediaEndTime = findViewById(R.id.media_endTime);
        mMediaExtraBox = findViewById(R.id.media_extra_box);
        mMediaResource = findViewById(R.id.media_resource);
        mMediaEpisode = findViewById(R.id.media_episode);

        //set listener
        mMediaBack.setOnClickListener(this);
        mMediaZoom.setOnClickListener(this);
        mMediaFastRewind10.setOnClickListener(this);
        mMediaPlay.setOnClickListener(this);
        mMediaFastForward10.setOnClickListener(this);
        mMediaNextEpisode.setOnClickListener(this);
        mMediaBottomPlay.setOnClickListener(this);
        mMediaResource.setOnClickListener(this);
        mMediaEpisode.setOnClickListener(this);

        mMediaSeekBar.setProgress(0);
        mMediaSeekBar.setMax(1000);
        mMediaSeekBar.setOnSeekBarChangeListener(this);

        mBottomBoxInitHeight = mMediaBottomBox.getLayoutParams().height;
    }

    @Override
    protected void onHandleMessage(Message msg) {
        super.onHandleMessage(msg);
        switch (msg.what) {
            case PlayConstant.Message.MESSAGE_UPDATE_PROGRESS:
                long pos = syncProgress();
                msg = mHandler.obtainMessage(PlayConstant.Message.MESSAGE_UPDATE_PROGRESS);
                mHandler.sendMessageDelayed(msg, 1000 - (pos % 1000));
                break;
            case PlayConstant.Message.MESSAGE_HIDE_BOX:
                mMediaTopBox.setVisibility(GONE);
                mMediaRightBox.setVisibility(GONE);
                mMediaBottomControllerBox.setVisibility(GONE);
                mMediaFastRewind10.setVisibility(INVISIBLE);
                mMediaFastForward10.setVisibility(INVISIBLE);
                mMediaPlay.setVisibility(isInPlaybackState() ? INVISIBLE : VISIBLE);
                break;
            case PlayConstant.Message.MESSAGE_SHOW_BOX:
                mMediaTopBox.setVisibility(VISIBLE);
                mMediaRightBox.setVisibility(isInPlaybackState() ? VISIBLE : GONE);
                mMediaBottomControllerBox.setVisibility(isInPlaybackState() ? VISIBLE : GONE);
                mMediaFastRewind10.setVisibility(!mIsPortrait && isInPlaybackState() ? VISIBLE : INVISIBLE);
                mMediaFastForward10.setVisibility(!mIsPortrait && isInPlaybackState() ? VISIBLE : INVISIBLE);
                mMediaPlay.setVisibility(isInPlaybackState() ? VISIBLE : (isShowLoadingBox() ? INVISIBLE : VISIBLE));
                if (getCurrentPlayState() == PlayerStatus.STATE_PLAYING) {
                    mHandler.sendEmptyMessageDelayed(PlayConstant.Message.MESSAGE_HIDE_BOX, delayTimeToHideControlbox);
                }
                break;
            case PlayConstant.Message.MESSAGE_SHOW_LOADING:
                mMediaLoadingPercent.setText("");
                mMediaLoadingSpeed.setText("");
                mMediaLoadingExtraInfo.setText("");
                mMediaLoadingBox.setVisibility(VISIBLE);
                break;
            case PlayConstant.Message.MESSAGE_HIDE_LOADING:
                mMediaLoadingPercent.setText("");
                mMediaLoadingSpeed.setText("");
                mMediaLoadingExtraInfo.setText("");
                mMediaLoadingBox.setVisibility(GONE);
                break;
            case PlayConstant.Message.MESSAGE_SHOW_MEIDA_PLAY:
                mMediaPlay.setVisibility(VISIBLE);
                break;
            case PlayConstant.Message.MESSAGE_HIDE_MEDIA_PLAY:
                mMediaPlay.setVisibility(INVISIBLE);
                break;
            case PlayConstant.Message.MESSAGE_SHOW_NEXT_EPISODE:
                mMediaNextEpisode.setVisibility(VISIBLE);
                break;
            case PlayConstant.Message.MESSAGE_HIDE_NEXT_EPISODE:
                mMediaNextEpisode.setVisibility(GONE);
                break;
            case PlayConstant.Message.MESSAGE_CHANGE_PLAY_STATUS:
                statusChange(msg.arg1, (boolean)msg.obj);
                break;
            case PlayConstant.Message.MESSAGE_UPDATE_LOADING_EXTRA_INFO:
                Bundle bundle = msg.getData();
                if (bundle != null) {
                    String loadingExtraInfo = bundle.getString(KEY_LOADING_EXTRA_INFO);
                    if (isShowLoadingBox() && currentConnectionType != OnConnectionChangeListener.ConnectionType.NONE) {
                        mMediaLoadingExtraInfo.setText(loadingExtraInfo);
                    }
                }
                break;
            case PlayConstant.Message.MESSAGE_CONFIGURATION_CHANGED_PORTRAOT:
                mMediaTopBox.setVisibility(GONE);
                mMediaBack.setVisibility(GONE);
                mMediaTitle.setVisibility(GONE);
                mMediaFreeTrial.setVisibility(GONE);
                mMediaRightBox.setVisibility(GONE);

                mMediaFastRewind10.setVisibility(INVISIBLE);
                mMediaFastForward10.setVisibility(INVISIBLE);
                mMediaPlay.setVisibility(isInPlaybackState() ? GONE : (isShowLoadingBox() ? GONE : VISIBLE));

                mMediaExtraBox.setVisibility(GONE);
                mMediaBottomPlay.setVisibility(VISIBLE);
                mMediaFullscreen.setVisibility(VISIBLE);
                mMediaEpisode.setVisibility(GONE);
                break;
            case PlayConstant.Message.MESSAGE_CONFIGURATION_CHANGED_LANDSCAPE:
                mMediaTopBox.setVisibility(VISIBLE);
                mMediaBack.setVisibility(VISIBLE);
                mMediaTitle.setVisibility(VISIBLE);
                mMediaFreeTrial.setVisibility(mIsFreeTrail ? VISIBLE : GONE);
                mMediaRightBox.setVisibility(VISIBLE);

                mMediaFastRewind10.setVisibility(isInPlaybackState() ? VISIBLE : INVISIBLE);
                mMediaFastForward10.setVisibility(isInPlaybackState() ? VISIBLE : INVISIBLE);
                mMediaPlay.setVisibility(isInPlaybackState() ? VISIBLE : (isShowLoadingBox() ? INVISIBLE : VISIBLE));

                mMediaExtraBox.setVisibility(mIsTrailer ? INVISIBLE : VISIBLE);
                mMediaBottomPlay.setVisibility(GONE);
                mMediaFullscreen.setVisibility(GONE);
                mMediaEpisode.setVisibility(mIsSeries && !mIsTrailer ? VISIBLE : GONE);
                break;
            case PlayConstant.Message.MESSAGE_RESET_PLAYER:
                mMediaTopBox.setVisibility(GONE);
                mMediaRightBox.setVisibility(GONE);
                mMediaFastForward10.setVisibility(INVISIBLE);
                mMediaFastRewind10.setVisibility(INVISIBLE);
                mMediaPlay.setVisibility(VISIBLE);
                mMediaBottomControllerBox.setVisibility(GONE);
                hideNextEpisode();
                hideLoadingBox();
                setIsTrailer(false);
                setSubtitleText("");
                mMediaTitle.setText("");
                setAllowDisplayNextEpisode(true);
                break;
            case PlayConstant.Message.MESSAGE_UPDATE_MEDIA_TITLE:
                Bundle bundleTitle = msg.getData();
                if (bundleTitle != null) {
                    String title = bundleTitle.getString(KEY_MEDIA_TITLE_INFO);
                    mMediaTitle.setText(title);
                }
                break;
        }
    }

    protected long syncProgress() {
        long position = getCurrentPosition();
        long duration = getDuration();
        if (mMediaSeekBar != null) {
            if (duration > 0) {
                long pos = 1000L * position / duration;
                mMediaSeekBar.setProgress((int) pos);
            }
            int percent = getCurrentBufferPercentage();
            mMediaSeekBar.setSecondaryProgress(percent * 10);
        }

        mMediaCurrentTime.setText(generateTime(position));
        mMediaEndTime.setText(generateTime(duration));
        if (isAllowDisplayNextEpisode && !mIsTrailer && mIsSeries && (!mIsCacheFinishFilm || !isFromCachePage) && (duration > position) && (duration - position <= PLAY_NEXT_EPISODE_THRESHOLD_VALUE)) {
            showNextEpisode();
        } else {
            hideNextEpisode();
        }
        return position;
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mIsPortrait) {
            showSystemUI();
            forbidTouch(true);
            mHandler.sendEmptyMessage(PlayConstant.Message.MESSAGE_CONFIGURATION_CHANGED_PORTRAOT);
        } else {
            hideSystemUI();
            forbidTouch(false);
            mHandler.sendEmptyMessage(PlayConstant.Message.MESSAGE_CONFIGURATION_CHANGED_LANDSCAPE);
        }
        setFullScreen(mIsPortrait);
    }

    //设置界面方向
    protected void setFullScreen(boolean isPortrait) {
        if (mActivity != null) {
            WindowManager.LayoutParams attrs = mActivity.getWindow().getAttributes();
            if (!isPortrait) {
                attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
                mActivity.getWindow().setAttributes(attrs);
                mActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            } else {
                attrs.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
                mActivity.getWindow().setAttributes(attrs);
                mActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            }
        }
        doOnConfigurationChanged(isPortrait);
    }


    //界面方向改变是刷新界面
    protected void doOnConfigurationChanged(final boolean portrait) {
        if (portrait) {
            mViewUtils.id(R.id.media_bottom_box).height(mBottomBoxInitHeight, false);
            mMediaTopBox.setPadding(0, 0, 0, 0);
            mMediaRightBox.setPadding(0, 0, 0, 0);
            mMediaBottomBox.setPadding(0, 0, 0, 0);
//            mSubtitlesView.setTextSize(DetailUtils.getTextSizeInPortrait(ContextProvider.getContext(), mCurrentSubSizePos));
        } else {
            mViewUtils.id(R.id.media_bottom_box).height(mBottomBoxInitHeight * 3 / 2, false);
            //全屏播放调整底部,右部和顶部布局的左右宽度
            mMediaTopBox.setPadding(mContext.getResources().getDimensionPixelOffset(R.dimen.sdk_vod_c_play_sm_36), 0, mContext.getResources().getDimensionPixelOffset(R.dimen.sdk_vod_c_play_sm_36), 0);
            mMediaRightBox.setPadding(mContext.getResources().getDimensionPixelOffset(R.dimen.sdk_vod_c_play_sm_36), 0, mContext.getResources().getDimensionPixelOffset(R.dimen.sdk_vod_c_play_sm_36), 0);
            mMediaBottomBox.setPadding(mContext.getResources().getDimensionPixelOffset(R.dimen.sdk_vod_c_play_sm_36), 0, mContext.getResources().getDimensionPixelOffset(R.dimen.sdk_vod_c_play_sm_36), 0);
//            mSubtitlesView.setTextSize(DetailUtils.getTextSizeInLandscape(ContextProvider.getContext(), mCurrentSubSizePos));
        }
    }

    @Override
    protected void onSingleTapUpEvent() {
        mHandler.removeMessages(PlayConstant.Message.MESSAGE_HIDE_BOX);
        if (VISIBLE == mMediaBottomControllerBox.getVisibility()) {
            mHandler.sendEmptyMessage(PlayConstant.Message.MESSAGE_HIDE_BOX);
        } else {
            mHandler.sendEmptyMessage(PlayConstant.Message.MESSAGE_SHOW_BOX);
        }
    }

    //进度条滑动监听
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        logger.d("onProgressChanged progress:%s fromUser:%s", progress, fromUser);
        if (!fromUser) {
            //不是用户拖动的，自动播放滑动的情况
        } else {
            long duration = getDuration();
            int position = (int) ((duration * progress * 1.0) / 1000);
            String time = generateTime(position);
            mViewUtils.id(R.id.media_currentTime).text(time);
        }
    }

    //开始拖动
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        isDragging = true;
        mHandler.removeMessages(PlayConstant.Message.MESSAGE_UPDATE_PROGRESS);
        mHandler.removeMessages(PlayConstant.Message.MESSAGE_HIDE_BOX);
    }

    //停止拖动
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        isDragging = false;
        isManualSeeking = true;
        if (mOnMediaControllerListener != null && OnConnectionChangeListener.ConnectionType.NONE == currentConnectionType) {
            mOnMediaControllerListener.onNetworkDisconnected(mActivity, new Action<Boolean>() {
                @Override
                public void call(Boolean isCachedFilm) {
                    if (!isCachedFilm) {
                        pause();
                    } else {
                        long duration = getDuration();
                        seekTo((int) ((duration * seekBar.getProgress() * 1.0) / 1000));
                    }
                }
            });
        } else {
            long duration = getDuration();
            seekTo((int) ((duration * seekBar.getProgress() * 1.0) / 1000));
        }
        mHandler.removeMessages(PlayConstant.Message.MESSAGE_UPDATE_PROGRESS);
        mHandler.sendEmptyMessageDelayed(PlayConstant.Message.MESSAGE_UPDATE_PROGRESS, 1000);
    }

    @Override
    public void onPreparedEvent(IMediaPlayer mp) {
        mIsPrepared = true;
        prepareTime = SystemClock.elapsedRealtime();
        hideLoadingBox();
    }

    @Override
    public void onCompletionEvent(IMediaPlayer mp) {
        if (lastPlayTime != -1) {
            calculatePlayTime();
        }
        if (lastSubPlayTime != -1 && isStatisticSub) {
            calculateSubPlayTime();
        }
        hideNextEpisode();
    }

    @Override
    public void onSeekCompleteEvent(IMediaPlayer mp) {
        isManualSeeking = false;
        hasSeeking = true;
        seekCompleteTime = SystemClock.elapsedRealtime();
    }

    @Override
    public void onErrorEvent(IMediaPlayer mp, int what, int extra) {
        if (VideoType.FFP_MSG_ERROR_997 != extra && VideoType.FFP_MSG_ERROR_998 != extra) {
            if (lastPlayTime != -1) {
                calculatePlayTime();
            }

            if (lastSubPlayTime != -1 && isStatisticSub) {
                calculateSubPlayTime();
            }
        }
        hideNextEpisode();
    }

    @Override
    public void onInfoEvent(IMediaPlayer mp, int what, int extra) {
        switch (what) {
            case IMediaPlayer.MEDIA_INFO_BUFFERING_START:
                isBuffering = true;
                if (isPlaying()) {
                    if (lastPlayTime != -1) {
                        calculatePlayTime();
                    }
                    if (lastSubPlayTime != -1 && isStatisticSub) {
                        calculateSubPlayTime();
                    }
                }
                bufferingStartTime = SystemClock.elapsedRealtime();
                if (mIsPrepared && !hasFirstPrepareBuffering) {
                    hasFirstPrepareBuffering = true;
                    prepareFirstBufferingTime = SystemClock.elapsedRealtime() - prepareTime;
                }

                if (hasSeeking) {
                    hasSeeking = false;
                    seekFirstBuffingTime = SystemClock.elapsedRealtime() - seekCompleteTime;
                }
                startBufferingPosition = mp.getCurrentPosition();
                showLoadingBox();
                if (OnConnectionChangeListener.ConnectionType.NONE == currentConnectionType) {
                    if (mOnMediaControllerListener != null) {
                        mOnMediaControllerListener.onNetworkDisconnected(mActivity, new Action<Boolean>() {
                            @Override
                            public void call(Boolean isCacheFilm) {
                                if (!isCacheFilm) {
                                    pause();
                                }
                            }
                        });
                    }
                }
                if (isManualSeeking) {
                    isBufferingBySeek = true;
                } else {
                    isBufferingBySeek = false;
                    if (mOnMediaControllerListener != null) mOnMediaControllerListener.onBufferingStart(mp);
                }
                break;
            case IMediaPlayer.MEDIA_INFO_BUFFERING_END:
                isBuffering = false;
                if (isPlaying()) {
                    if (lastPlayTime != -1) {
                        lastPlayTime = SystemClock.elapsedRealtime();
                    }
                    if (lastSubPlayTime != -1 && isStatisticSub) {
                        lastSubPlayTime = SystemClock.elapsedRealtime();
                    }
                }
                bufferingEndTime = SystemClock.elapsedRealtime();
                bufferingTime = bufferingEndTime - bufferingStartTime;
                hideLoadingBox();
                if (!isBufferingBySeek) {
                    if (mOnMediaControllerListener != null) {
                        mOnMediaControllerListener.onBufferingEnd(mp, mIsPrepared, bufferingStartTime,
                                bufferingTime, prepareFirstBufferingTime, seekFirstBuffingTime, startBufferingPosition);
                    }
                }
                break;
            case IMediaPlayer.MEDIA_INFO_VIDEO_SEEK_RENDERING_START:
                if (getCurrentPlayState() == PlayerStatus.STATE_PLAYING) {
                    mHandler.sendEmptyMessageDelayed(PlayConstant.Message.MESSAGE_HIDE_BOX, delayTimeToHideControlbox);
                } else if (getCurrentPlayState() == PlayerStatus.STATE_PAUSED) {
                    mHandler.removeMessages(PlayConstant.Message.MESSAGE_HIDE_BOX);
                }
                break;
            case VideoType.MEDIA_INFO_PLAYER_TYPE:
                if (mOnMediaControllerListener != null) mOnMediaControllerListener.onPlayerType(extra);
                break;
        }
    }

    @Override
    public void onBufferingUpdateEvent(IMediaPlayer mp, int percent) {

    }

    @Override
    public int getPlayerType() {
        return PlayerHelper.getInstance().getPlayerType(mIsTrailer);
    }

    //状态改变同步UI
    protected void statusChange(int newState, boolean isResetLastPlayTime) {
        PlayerStatus newPlayerState = PlayerStatus.values()[newState];
        logger.i("currState:%s newPlayerState:%s", getCurrentPlayState().name(), newPlayerState.name());
        switch (newPlayerState) {
            case STATE_IDLE:
                updatePausePlay(PlayerStatus.STATE_IDLE);
                mHandler.removeMessages(PlayConstant.Message.MESSAGE_UPDATE_PROGRESS);
                mHandler.removeMessages(PlayConstant.Message.MESSAGE_SHOW_BOX);
                mHandler.removeMessages(PlayConstant.Message.MESSAGE_HIDE_BOX);
                mHandler.removeMessages(PlayConstant.Message.MESSAGE_RESET_PLAYER);
                mHandler.sendEmptyMessage(PlayConstant.Message.MESSAGE_RESET_PLAYER);
                break;
            case STATE_PREPARING:
                break;
            case STATE_PREPARED:
                lastPlayTime = SystemClock.elapsedRealtime();
                if (isStatisticSub) lastSubPlayTime = SystemClock.elapsedRealtime();
                break;
            case STATE_PAUSED:
                if (lastPlayTime != -1) {
                    calculatePlayTime();
                }
                if (lastSubPlayTime != -1 && isStatisticSub) {
                    calculateSubPlayTime();
                }
                updatePausePlay(PlayerStatus.STATE_PAUSED);
                mHandler.removeMessages(PlayConstant.Message.MESSAGE_UPDATE_PROGRESS);
                mHandler.removeMessages(PlayConstant.Message.MESSAGE_HIDE_BOX);
                mHandler.removeMessages(PlayConstant.Message.MESSAGE_SHOW_BOX);
                mHandler.sendEmptyMessage(PlayConstant.Message.MESSAGE_SHOW_BOX);
                break;
            case STATE_PLAYING:
                if (isResetLastPlayTime) {
                    lastPlayTime = SystemClock.elapsedRealtime();
                    if (isStatisticSub) lastSubPlayTime = SystemClock.elapsedRealtime();
                }
                updatePausePlay(PlayerStatus.STATE_PLAYING);
                mHandler.removeMessages(PlayConstant.Message.MESSAGE_UPDATE_PROGRESS);
                mHandler.sendEmptyMessage(PlayConstant.Message.MESSAGE_UPDATE_PROGRESS);
                mHandler.removeMessages(PlayConstant.Message.MESSAGE_HIDE_BOX);
                mHandler.removeMessages(PlayConstant.Message.MESSAGE_SHOW_BOX);
                mHandler.sendEmptyMessage(PlayConstant.Message.MESSAGE_SHOW_BOX);
                break;
            case STATE_COMPLETED:
//                resetPlayer();
                break;
            case STATE_ERROR:
//                resetPlayer();
                break;
        }
    }

    /**
     * 更新播放、暂停和停止按钮
     */
    protected void updatePausePlay(PlayerStatus state) {
        if (state == PlayerStatus.STATE_PLAYING) {
            mViewUtils.id(R.id.media_bottom_play).image(R.drawable.sdk_vod_c_play_ic_play);
            mViewUtils.id(R.id.media_play).image(R.drawable.sdk_vod_c_play_ic_play);
        } else {
            mViewUtils.id(R.id.media_bottom_play).image(R.drawable.sdk_vod_c_play_ic_pause);
            mViewUtils.id(R.id.media_play).image(R.drawable.sdk_vod_c_play_ic_pause);
        }
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        int id = v.getId();
        if (R.id.media_bottom_play == id) {
            switchPlaybackStatus();
        } else if (R.id.media_zoom == id) {
            toggleRatio();
        } else if (R.id.media_episode == id) {
            showEpisodesDialog();
        } else if (R.id.media_back == id) {
            clickBack();
        } else if (R.id.media_play == id) {
            clickMediaPlay();
        } else if (R.id.media_resource == id) {
            showResourceDialog();
        } else if (R.id.media_fast_forward_10 == id) {
            handleFixedStep(10000);
        } else if (R.id.media_fast_rewind_10 == id) {
            handleFixedStep(-10000);
        } else if (R.id.media_next_episode == id) {
            clickNextEpisode(mActivity);
        }
    }

    protected void switchPlaybackStatus() {
        if (isPlaying()) {
            pause();
        } else {
            if ((OnConnectionChangeListener.ConnectionType.MOBILE == networkReceiver.getType()) && (mOnMediaControllerListener != null && !mOnMediaControllerListener.isMobileTraffic())) {
                mOnMediaControllerListener.onNetworkChange(mActivity, new Action<Boolean>() {
                    @Override
                    public void call(Boolean o) {
                        if (o) {
                            resumePlay();
                        }
                    }
                });
            } else if (OnConnectionChangeListener.ConnectionType.NONE == networkReceiver.getType() && mOnMediaControllerListener != null) {
                mOnMediaControllerListener.onNetworkDisconnected(mActivity, new Action<Boolean>() {
                    @Override
                    public void call(Boolean isCacheFilm) {
                        if (isCacheFilm) {
                            resumePlay();
                        }
                    }
                });
            } else {
                resumePlay();
            }
        }
    }

    protected void toggleRatio() {
        if (mVideoPlayer != null) {
            int currentAspectRatio = mVideoPlayer.getAspectRatioMode();
            if (currentAspectRatio == VideoType.AR_ASPECT_FIT_PARENT) {
                mVideoPlayer.setAspectRatioMode(VideoType.AR_ASPECT_FILL_PARENT);
                mMediaZoom.setBackgroundResource(R.drawable.sdk_vod_c_play_ic_zoom_in);
            } else if (currentAspectRatio == VideoType.AR_ASPECT_FILL_PARENT) {
                mVideoPlayer.setAspectRatioMode(VideoType.AR_ASPECT_FIT_PARENT);
                mMediaZoom.setBackgroundResource(R.drawable.sdk_vod_c_play_ic_zoom_out);
            } else {
                logger.e( "click ratio, current aspect ratio is %s", currentAspectRatio);
            }
            if (mOnMediaControllerListener != null) mOnMediaControllerListener.onToggleRatio();
        }
    }

    protected void showEpisodesDialog() {
        if (mOnMediaControllerListener != null) mOnMediaControllerListener.showEpisodesDialog(mActivity, ((FragmentActivity)mActivity).getSupportFragmentManager());
    }

    protected void showResourceDialog() {
        if (mOnMediaControllerListener != null) mOnMediaControllerListener.showResourceDialog(mActivity, ((FragmentActivity)mActivity).getSupportFragmentManager());
    }

    protected void clickMediaPlay() {
        if (isInPlaybackState()) {
            //播放中是播放暂停切换
            switchPlaybackStatus();
        } else {
            if (mOnMediaControllerListener != null) mOnMediaControllerListener.onPreparePlay(mActivity);
        }
    }

    protected void clickBack() {
        if (mRotationLocked) {
            ToastUtils.showToast(getContext(), R.string.sdk_vod_c_play_str_screen_locked);
            return;
        }
        if (mIsPortrait || (isFromCachePage && mIsCacheFinishFilm)) {
            if (mOnMediaControllerListener != null) mOnMediaControllerListener.finish();
        } else {
            exitFullScreen();
            if (mOnMediaControllerListener != null) mOnMediaControllerListener.onResetPlayer();
        }
    }

    protected void clickNextEpisode(Activity activity) {
        if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
            return;
        }
        isAllowDisplayNextEpisode = false;
        hideNextEpisode();
        if (null != mOnMediaControllerListener) {
            mOnMediaControllerListener.onNextEpisode(activity);
        }
    }

    //处理快进快退固定时长
    protected void handleFixedStep(long step) {
        if (!isInPlaybackState() || step == 0) {
            return;
        }

        mHandler.removeMessages(PlayConstant.Message.MESSAGE_UPDATE_PROGRESS);
        mHandler.removeMessages(PlayConstant.Message.MESSAGE_HIDE_BOX);

        long currentPosition = getCurrentPosition();
        long duration = getDuration();
        long finalPosition = currentPosition;
        if (duration > 0) {
            if (step > 0) {
                if (duration - currentPosition > step) {
                    finalPosition += step;
                } else {
                    finalPosition = duration;
                }
            } else {
                if (currentPosition > Math.abs(step)) {
                    finalPosition -= Math.abs(step);
                } else {
                    finalPosition = 0;
                }
            }
        } else {
            logger.i("handleFixedStep duration[%d] is invalid", duration);
        }

        if (finalPosition != currentPosition) {
            seekTo((int)finalPosition);
//            if (onEventCallback != null) onEventCallback.onSeek(finalPosition);
        }

        mHandler.removeMessages(PlayConstant.Message.MESSAGE_UPDATE_PROGRESS);
        mHandler.sendEmptyMessage(PlayConstant.Message.MESSAGE_UPDATE_PROGRESS);
    }

    public void hideLoadingBox() {
        mHandler.sendEmptyMessage(PlayConstant.Message.MESSAGE_HIDE_LOADING);
    }

    public void showLoadingBox() {
        mHandler.sendEmptyMessage(PlayConstant.Message.MESSAGE_SHOW_LOADING);
    }
    public boolean isShowLoadingBox() {
        return this.mMediaLoadingBox.getVisibility() == VISIBLE;
    }

    public void hideMediaPlay() {
        mHandler.sendEmptyMessage(PlayConstant.Message.MESSAGE_HIDE_MEDIA_PLAY);
    }

    public void showMediaPlay() {
        mHandler.sendEmptyMessage(PlayConstant.Message.MESSAGE_SHOW_MEIDA_PLAY);
    }

    protected void showNextEpisode() {
        mHandler.sendEmptyMessage(PlayConstant.Message.MESSAGE_SHOW_NEXT_EPISODE);
    }

    protected void hideNextEpisode() {
        mHandler.sendEmptyMessage(PlayConstant.Message.MESSAGE_HIDE_NEXT_EPISODE);
    }

    //获取实际的播放时间（去掉暂停、卡顿等时间）
    public long getActualPlaybackTime() {
        if (!isBuffering && isPlaying() && lastPlayTime != -1) {
            calculatePlayTime();
        }
        return actualTime;
    }

    private void calculatePlayTime() {
        actualTime += (SystemClock.elapsedRealtime() - lastPlayTime);
        lastPlayTime = SystemClock.elapsedRealtime();
    }

    public void resetPlaybackTime() {
        actualTime = 0;
        lastPlayTime = -1;
    }

    public void setStatisticSub(boolean isStatisticSub) {
        this.isStatisticSub = isStatisticSub;
        if (!isStatisticSub && !isBuffering && isPlaying() && lastSubPlayTime != -1) {
            subActualTime += (SystemClock.elapsedRealtime() - lastSubPlayTime);
        }
        lastSubPlayTime = SystemClock.elapsedRealtime();
    }

    public long getActualSubtitleTime() {
        if (!isBuffering && isPlaying() && lastSubPlayTime != -1 && isStatisticSub) {
            calculateSubPlayTime();
        }
        return subActualTime;
    }

    private void calculateSubPlayTime() {
        subActualTime += (SystemClock.elapsedRealtime() - lastSubPlayTime);
        lastSubPlayTime = SystemClock.elapsedRealtime();
    }

    public void resetSubtitleTime(boolean isResetLastTime) {
        subActualTime = 0;
        lastSubPlayTime = isResetLastTime ? SystemClock.elapsedRealtime() : -1;
    }

    public void setLoadingPercent(String loadingPercent) {
        if (isShowLoadingBox() && currentConnectionType != OnConnectionChangeListener.ConnectionType.NONE) {
            this.mMediaLoadingPercent.setText(loadingPercent);
        }
    }

    public void setLoadingSpeed(String loadingSpeed) {
        if (isShowLoadingBox() && currentConnectionType != OnConnectionChangeListener.ConnectionType.NONE) {
            this.mMediaLoadingSpeed.setText(loadingSpeed);
        }
    }

    public void setLoadingExtraInfo(String txt) {
        Bundle bundle = new Bundle();
        bundle.putString(KEY_LOADING_EXTRA_INFO, txt);
        Message message = mHandler.obtainMessage(PlayConstant.Message.MESSAGE_UPDATE_LOADING_EXTRA_INFO);
        message.setData(bundle);
        mHandler.sendMessage(message);
    }

    public void setMediaTitle(String title) {
        Bundle bundle = new Bundle();
        bundle.putString(KEY_MEDIA_TITLE_INFO, title);
        Message message = mHandler.obtainMessage(PlayConstant.Message.MESSAGE_UPDATE_MEDIA_TITLE);
        message.setData(bundle);
        mHandler.sendMessage(message);
    }

    public void setIsCachedFilm(boolean isCacheFinishFilm) {
        this.mIsCacheFinishFilm = isCacheFinishFilm;
    }

    public void setIsFromCachePage(boolean isFromCachePage) {
        this.isFromCachePage = isFromCachePage;
    }
    public void setAllowDisplayNextEpisode(boolean isAllowDisplayNextEpisode) {
        this.isAllowDisplayNextEpisode = isAllowDisplayNextEpisode;
    }

    public void setIsTrailer(boolean isTrailer) {
        this.mIsTrailer = isTrailer;
        if (mVideoPlayer != null) {
            mVideoPlayer.setTrailer(isTrailer);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isAllowDisplayNextEpisode = true;
    }
}
