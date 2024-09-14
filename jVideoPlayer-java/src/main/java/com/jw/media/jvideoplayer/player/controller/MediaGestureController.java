package com.jw.media.jvideoplayer.player.controller;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.gyf.immersionbar.ImmersionBar;
import com.gyf.immersionbar.BarHide;

import com.jw.media.jvideoplayer.java.R;
import com.jw.media.jvideoplayer.lib.log.Logger;
import com.jw.media.jvideoplayer.lib.log.LoggerFactory;
import com.jw.media.jvideoplayer.lib.provider.ContextProvider;
import com.jw.media.jvideoplayer.lib.utils.ToastUtils;
import com.jw.media.jvideoplayer.player.play.PlayConstant;
import com.jw.media.jvideoplayer.player.base.PlayerStatus;

/**
 * Created by Joyce.wang on 2024/3/12 19:29
 *
 * @Description Media Gesture Controller handles gestures during playback,
 *             including full-screen, small-screen, brightness, volume, and seeking.
 */
public abstract class MediaGestureController extends BaseMediaController
        implements View.OnClickListener, View.OnTouchListener, ViewTreeObserver.OnGlobalLayoutListener {
    private static Logger logger = LoggerFactory.getLogger(MediaGestureController.class.getSimpleName());

    //Brightness controls（亮度）
    protected LinearLayout mMediaBrightness;//亮度调整整体布局view
    protected TextView mMediaBrightnessTv;//亮度百分比

    //Volume controls（调节声音）
    protected LinearLayout mMediaVolume;//调节声音整体布局view
    protected ImageView mMediavolumeIv;//声音图片view
    protected TextView mMediaVolumeTv;//声音调节百分比

    //Seek controls（快进快退）
    protected LinearLayout mMediaFastForwardBox;//快进快退整体布局view
    protected TextView mMediaFastForwardTv;//快进快退具体值（s）
    protected TextView mMediaFastForwardTargetTv;//快进快退target（在总时长中的位置）
    protected TextView mMediaFastForwardAllTv;//总时长展示view

    protected ImageView mMediaOrientationLock;//屏幕锁
    protected ImageView mMediaFullscreen;

    protected boolean isProgressSlideEnable = true;
    protected boolean isVolumeSlideEnable = true;//volume sliding
    protected boolean isBrightnessSlideEnable = true;//brightness slide
    protected boolean isForbidTouch;//禁止触摸，默认可以触摸，true为禁止false为可触摸

    protected long newPosition = -1;//滑动进度条得到的新位置，和当前播放位置是有区别的,newPosition =0也会调用设置的，故初始化值为-1
    protected boolean mRotationLocked = false;//屏幕是否有上锁
    protected OrientationEventListener orientationEventListener;//Activity界面方向监听
    protected int volume;//当前声音大小
    protected float brightness;//当前亮度大小
    protected GestureDetector gestureDetector;
    protected boolean isManualSeeking = false;//是否是手动seek
    protected ImmersionBar mImmersionBar;//状态栏沉浸

    public MediaGestureController(@NonNull Context context) {
        super(context);
    }

    public MediaGestureController(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MediaGestureController(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MediaGestureController(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    protected Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            onHandleMessage(msg);
        }
    };

    protected void onHandleMessage(Message msg) {
        switch (msg.what) {
            case PlayConstant.Message.MESSAGE_HIDE_GESTURE_BOX:
                mViewUtils.id(R.id.media_volume).gone();
                mViewUtils.id(R.id.media_brightness).gone();
                mViewUtils.id(R.id.media_fastForward_box).gone();

                break;
            //滑动完成，设置播放进度
            case PlayConstant.Message.MESSAGE_SEEK_NEW_POSITION:
                if (newPosition >= 0) {
                    seekTo((int) newPosition);
                    newPosition = -1;
                }
                break;
            //滑动完成，隐藏滑动提示的box
            case PlayConstant.Message.MESSAGE_HIDE_CENTER_BOX:
                mViewUtils.id(R.id.media_volume).gone();
                mViewUtils.id(R.id.media_brightness).gone();
                mViewUtils.id(R.id.media_fastForward_box).gone();
                break;
            case PlayConstant.Message.MESSAGE_ENTER_FULL_SCREEN_PLAY:
                if (getScreenOrientation() != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE && getScreenOrientation() != ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE) {
                    orientationEventListener.enable();
//                  mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    mVideoPlayer.enterFullScreen(mActivity);
                }
                break;
            case PlayConstant.Message.MESSAGE_EXIT_FULL_SCREEN_PLAY:
                if (getScreenOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE || getScreenOrientation() == ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE) {
                    orientationEventListener.disable();
//                    mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    mVideoPlayer.exitFullScreen(mActivity);
                }
                break;
        }
    }

    @Override
    protected void init(Context context) {
        super.init(context);
        //中间触摸提示 box

        //亮度调节
        mMediaBrightness = findViewById(R.id.media_brightness);
        mMediaBrightnessTv = findViewById(R.id.media_brightness_tv);

        //调节声音
        mMediaVolume = findViewById(R.id.media_volume);
        mMediavolumeIv = findViewById(R.id.media_volume_iv);
        mMediaVolumeTv = findViewById(R.id.media_volume_tv);

        //快进快退
        mMediaFastForwardBox = findViewById(R.id.media_fastForward_box);
        mMediaFastForwardTv = findViewById(R.id.media_fastForward_tv);
        mMediaFastForwardTargetTv = findViewById(R.id.media_fastForward_target_tv);
        mMediaFastForwardAllTv = findViewById(R.id.media_fastForward_all_tv);

        mMediaOrientationLock = findViewById(R.id.media_orientation_lock);
        mMediaFullscreen = findViewById(R.id.media_fullscreen);

        mMediaOrientationLock.setOnClickListener(this);
        mMediaFullscreen.setOnClickListener(this);
        mRoot.setClickable(true);//不设置为true，会导致GestureDetector监听不生效
        mRoot.setOnTouchListener(this);

        gestureDetector = new GestureDetector(ContextProvider.getContext(), new PlayerGestureListener());

        orientationEventListener = new OrientationEventListener(mActivity) {
            @Override
            public void onOrientationChanged(int orientation) {
                if (orientation > 10 && orientation < 170) {
                    mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
                } else if (orientation > 190 && orientation < 350) {
                    mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                }
            }
        };

        //初始化沉浸式状态栏
        if (isStatusBarEnabled()) {
            ImmersionBar immersionBar = statusBarConfig();
            if (immersionBar != null) immersionBar.init();
        }
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    /**
     * 初始化沉浸式状态栏
     */
    protected ImmersionBar statusBarConfig() {
        if (mActivity == null) {
            logger.e("statusBarConfig, activity is null.");
            return null;
        }
        //在BaseActivity里初始化
        mImmersionBar = ImmersionBar.with(mActivity)
//                .statusBarDarkFont(statusBarDarkFont())    //默认状态栏字体颜色为黑色
                .keyboardEnable(false, WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN
                        | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);  //解决软键盘与底部输入框冲突问题，默认为false，还有一个重载方法，可以指定软键盘mode
        //必须设置View树布局变化监听，否则软键盘无法顶上去，还有模式必须是SOFT_INPUT_ADJUST_PAN
        mActivity.getWindow().getDecorView().getViewTreeObserver().addOnGlobalLayoutListener(this);
        return mImmersionBar;
    }
    /**
     * 是否使用沉浸式状态栏
     */
    protected boolean isStatusBarEnabled() {
        return true;
    }

    /**
     * 获取状态栏沉浸的配置对象
     */
    protected ImmersionBar getStatusBarConfig() {
        return mImmersionBar;
    }

    /**
     * Show system UI (status bar and navigation bar).
     */
    protected void showSystemUI() {
        if (getStatusBarConfig() != null) {
            getStatusBarConfig().fullScreen(false).hideBar(BarHide.FLAG_SHOW_BAR).init();
        }
    }

    /**
     * Hide system UI (status bar and navigation bar).
     */
    protected void hideSystemUI() {
        if (getStatusBarConfig() != null) {
            getStatusBarConfig().fullScreen(true).hideBar(BarHide.FLAG_HIDE_BAR).init();
        }
    }

    /**
     * Hide the full-screen button.
     */
    public void hideFullScreenButton() {
        this.mMediaFullscreen.setVisibility(GONE);
    }

    /**
     * Show the full-screen button.
     */
    public void showFullScreenButton() {
        this.mMediaFullscreen.setVisibility(VISIBLE);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (R.id.media_fullscreen == id) {
            clickMediaFullScreen();
        } else if (R.id.media_orientation_lock == id) {
            toggleLockRotation();
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                break;
        }
        if (gestureDetector.onTouchEvent(motionEvent)) {
            return true;
        }
        // 处理手势结束
        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_UP:
                endGesture();
                break;
        }
        return false;
    }
    /**
     * 手势结束
     */
    private void endGesture() {
        isManualSeeking = true;
        volume = -1;
        brightness = -1f;
        mHandler.removeMessages(PlayConstant.Message.MESSAGE_HIDE_GESTURE_BOX);
        mHandler.sendEmptyMessageDelayed(PlayConstant.Message.MESSAGE_HIDE_GESTURE_BOX, 500);
        if (newPosition >= 0) {
            mHandler.removeMessages(PlayConstant.Message.MESSAGE_SEEK_NEW_POSITION);
            mHandler.sendEmptyMessage(PlayConstant.Message.MESSAGE_SEEK_NEW_POSITION);
        }
        mHandler.removeMessages(PlayConstant.Message.MESSAGE_HIDE_CENTER_BOX);
        mHandler.sendEmptyMessageDelayed(PlayConstant.Message.MESSAGE_HIDE_CENTER_BOX, 500);
    }

    protected void enableProgressSlide(boolean enable) {
        this.isProgressSlideEnable = enable;
    }

    //是否禁止触摸
    public void forbidTouch(boolean forbidTouch) {
        this.isForbidTouch = forbidTouch;
    }

    //设置是否禁止滑动调节声音
    protected void enableVolumeSlide(boolean enable) {
        this.isVolumeSlideEnable = enable;
    }

    //设置是否禁止滑动调节亮度
    protected void enableBrightnessSlide(boolean enable) {
        this.isBrightnessSlideEnable = enable;
    }


    private class PlayerGestureListener extends GestureDetector.SimpleOnGestureListener {
        /**
         * 是否是按下的标识，默认为其他动作，true为按下标识，false为其他动作
         */
        private boolean isDownTouch;
        /**
         * 是否声音控制,默认为亮度控制，true为声音控制，false为亮度控制
         */
        private boolean isVolume;
        /**
         * 是否横向滑动，默认为纵向滑动，true为横向滑动，false为纵向滑动
         */
        private boolean isLandscape;
        /**
         * 双击
         */
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            /**视频视窗双击事件*/
//            switchPortraitOrLandscape();
            return true;
        }
        /**
         * 按下
         */
        @Override
        public boolean onDown(MotionEvent e) {
            isDownTouch = true;
            return super.onDown(e);
        }
        /**
         * 滑动
         */
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            float mOldX = e1.getX(), mOldY = e1.getY();
            float deltaY = mOldY - e2.getY();
            float deltaX = mOldX - e2.getX();
            if (isDownTouch) {
                isLandscape = Math.abs(distanceX) >= Math.abs(distanceY);
                isVolume = mOldX > mScreenWidth * 0.5f;
                isDownTouch = false;
            }

            if (isLandscape) {
                /**进度设置*/
                onProgressSlide(-deltaX / getWidth());
                logger.d("onScroll horizonally");
            } else {
                if (!isForbidTouch) {
                    float percent = deltaY / getHeight();
                    if (isVolume) {
                        /**声音设置*/
                        onVolumeSlide(percent);
                        logger.d("left onScroll vertically %s", percent);
                    } else {
                        /**亮度设置*/
                        onBrightnessSlide(percent);
                        logger.d("right onScroll vertically %s", percent);
                    }
                }
            }
            return super.onScroll(e1, e2, distanceX, distanceY);
        }
        /**
         * 单击
         */
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            /**视频视窗单击事件*/
            onSingleTapUpEvent();
            return true;
        }
    }

    //全屏播放
    public void enterFullScreen() {
        logger.i("toggle full screen");
        mHandler.sendEmptyMessage(PlayConstant.Message.MESSAGE_ENTER_FULL_SCREEN_PLAY);
    }
    /**
     *@description 退出全屏播放
     *@return
     */
    public void exitFullScreen() {
        if (mRotationLocked) {
            ToastUtils.showToast(getContext(), R.string.sdk_vod_c_play_str_screen_locked);
            return;
        }
        mHandler.sendEmptyMessage(PlayConstant.Message.MESSAGE_EXIT_FULL_SCREEN_PLAY);
    }

    /**
     * 获取界面方向
     */
    public int getScreenOrientation() {
        Activity mActivity = (Activity) getContext();
        int rotation = mActivity.getWindowManager().getDefaultDisplay().getRotation();
        DisplayMetrics dm = new DisplayMetrics();
        mActivity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        int orientation;
        // if the device's natural orientation is portrait:
        if ((rotation == Surface.ROTATION_0
                || rotation == Surface.ROTATION_180) && height > width ||
                (rotation == Surface.ROTATION_90
                        || rotation == Surface.ROTATION_270) && width > height) {
            switch (rotation) {
                case Surface.ROTATION_0:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    break;
                case Surface.ROTATION_90:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    break;
                case Surface.ROTATION_180:
                    orientation =
                            ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                    break;
                case Surface.ROTATION_270:
                    orientation =
                            ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                    break;
                default:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    break;
            }
        }
        // if the device's natural orientation is landscape or if the device
        // is square:
        else {
            switch (rotation) {
                case Surface.ROTATION_0:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    break;
                case Surface.ROTATION_90:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    break;
                case Surface.ROTATION_180:
                    orientation =
                            ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                    break;
                case Surface.ROTATION_270:
                    orientation =
                            ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                    break;
                default:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    break;
            }
        }
        return orientation;
    }

    //快进或者快退滑动改变进度
    private void onProgressSlide(float percent) {
        if (!isProgressSlideEnable) {
            return;
        }
        long position = getCurrentPosition();
        long duration = getDuration();
        long deltaMax = Math.min(100 * 1000, duration - position);
        long delta = (long) (deltaMax * percent);
        newPosition = delta + position;
        if (newPosition > duration) {
            newPosition = duration;
        } else if (newPosition <= 0) {
            newPosition = 0;
            delta = -position;
        }
        int showDelta = (int) delta / 1000;
        if (showDelta != 0) {
            mViewUtils.id(R.id.media_fastForward_box).visible();
            String text = showDelta > 0 ? ("+" + showDelta) : "" + showDelta;
            mViewUtils.id(R.id.media_fastForward_tv).text(text + "s");
            mViewUtils.id(R.id.media_fastForward_target_tv).text(generateTime(newPosition) + "/");
            mViewUtils.id(R.id.media_fastForward_all_tv).text(generateTime(duration));
        }
    }

    //时长格式化显示
    protected String generateTime(long time) {
        int totalSeconds = (int) (time / 1000);
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;
        return hours > 0 ? String.format("%02d:%02d:%02d", hours, minutes, seconds) : String.format("%02d:%02d", minutes, seconds);
    }

    //滑动改变声音大小
    private void onVolumeSlide(float percent) {
        if (!isVolumeSlideEnable) {
            return;
        }
        if (volume == -1) {
            volume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            if (volume < 0)
                volume = 0;
        }
        int index = (int) (percent * mMaxVolume) + volume;
        if (index > mMaxVolume)
            index = mMaxVolume;
        else if (index < 0)
            index = 0;

        // 变更声音
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, index, 0);

        // 变更进度条
        int i = (int) (index * 1.0 / mMaxVolume * 100);
        String s = i + "%";
        if (i == 0) {
            s = "off";
        }
        // 显示
        mViewUtils.id(R.id.media_volume_iv).image(i == 0 ? R.drawable.sdk_vod_c_play_ic_volume_off : R.drawable.sdk_vod_c_play_ic_volume_up);
        mViewUtils.id(R.id.media_brightness).gone();
        mViewUtils.id(R.id.media_volume).visible();
        mViewUtils.id(R.id.media_volume_tv).text(s).visible();
    }

    //亮度滑动改变亮度
    private void onBrightnessSlide(float percent) {
        if (!isBrightnessSlideEnable) {
            return;
        }
        Activity mActivity = (Activity) mContext;
        if (brightness < 0) {
            brightness = mActivity.getWindow().getAttributes().screenBrightness;
            if (brightness <= 0.00f) {
                brightness = 0.50f;
            } else if (brightness < 0.01f) {
                brightness = 0.01f;
            }
        }
        mViewUtils.id(R.id.media_brightness).visible();
        WindowManager.LayoutParams lpa = mActivity.getWindow().getAttributes();
        lpa.screenBrightness = brightness + percent;
        if (lpa.screenBrightness > 1.0f) {
            lpa.screenBrightness = 1.0f;
        } else if (lpa.screenBrightness < 0.01f) {
            lpa.screenBrightness = 0.01f;
        }
        mViewUtils.id(R.id.media_brightness_tv).text(((int) (lpa.screenBrightness * 100)) + "%");
        mActivity.getWindow().setAttributes(lpa);
    }

    public void clickMediaFullScreen() {
        if (mRotationLocked) {
            ToastUtils.showToast(getContext(), R.string.sdk_vod_c_play_str_screen_locked);
            return;
        }

        if (getScreenOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE || getScreenOrientation() == ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE) {
            orientationEventListener.disable();
//            mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            mVideoPlayer.exitFullScreen(mActivity);
        } else {
            orientationEventListener.enable();
//            mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            mVideoPlayer.enterFullScreen(mActivity);
        }
    }

    /**
     * 锁定旋转方向
     */
    public void toggleLockRotation() {
        if (orientationEventListener == null) {
            return;
        }

        if (mRotationLocked) {
            mRotationLocked = false;
            orientationEventListener.enable();
            mMediaOrientationLock.setImageResource(R.drawable.sdk_vod_c_play_ic_lock);
        } else {
            mRotationLocked = true;
            orientationEventListener.disable();
            mMediaOrientationLock.setImageResource(R.drawable.sdk_vod_c_play_ic_locked);
        }
    }

    @Override
    public void setStateAndUi(PlayerStatus state, boolean isResetLastPlayTime) {
        Message message = mHandler.obtainMessage(PlayConstant.Message.MESSAGE_CHANGE_PLAY_STATUS);
        message.arg1 = state.ordinal();
        message.obj = isResetLastPlayTime;
        mHandler.sendMessage(message);
    }

    @Override
    public void onGlobalLayout() {

    }

    //处理视频窗口单击事件
    protected abstract void onSingleTapUpEvent();

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mHandler != null) mHandler.removeCallbacksAndMessages(null);
        if (orientationEventListener != null) orientationEventListener.disable();
    }
}
