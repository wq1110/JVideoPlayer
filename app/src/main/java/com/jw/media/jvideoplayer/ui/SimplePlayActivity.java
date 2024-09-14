package com.jw.media.jvideoplayer.ui;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.View;
import android.view.ViewTreeObserver;

import androidx.annotation.NonNull;

import com.jw.media.jvideoplayer.BR;
import com.jw.media.jvideoplayer.R;
import com.jw.media.jvideoplayer.databinding.ActivitySimplePlayBinding;
import com.jw.media.jvideoplayer.lib.log.Logger;
import com.jw.media.jvideoplayer.lib.log.LoggerFactory;
import com.jw.media.jvideoplayer.mvx.mvvm.MVVMBaseActivity;
import com.jw.media.jvideoplayer.player.SdkServiceManager;
import com.jw.media.jvideoplayer.player.controller.PureMediaController;
import com.jw.media.jvideoplayer.player.model.PlayParam;
import com.jw.media.jvideoplayer.player.play.IPlay;
import com.jw.media.jvideoplayer.player.request.Callback;
import com.jw.media.jvideoplayer.viewmodel.SimplePlayViewModel;

import me.jessyan.autosize.AutoSizeConfig;
import me.jessyan.autosize.utils.ScreenUtils;

/**
 * Created by Joyce.wang on 2024/9/13 8:57
 *
 * @Description TODO
 */
public class SimplePlayActivity extends MVVMBaseActivity<SimplePlayViewModel, ActivitySimplePlayBinding>
        implements ViewTreeObserver.OnGlobalLayoutListener {
    private static Logger logger = LoggerFactory.getLogger(SimplePlayActivity.class.getSimpleName());

    private PureMediaController mMediaController;
    private HandlerThread mThread;
    private Handler mWorkHandler;

    @Override
    protected int getBindingVariable() {
        return BR.viewModel;
    }

    @Override
    public int getLayoutID() {
        return R.layout.activity_simple_play;
    }

    @Override
    protected void initData() {
        mThread = new HandlerThread("simple play thread");
        mThread.start();
        mWorkHandler = new Handler(mThread.getLooper());

        mViewModel.initData(getIntent());
        initListener();

        mMediaController = new PureMediaController(this);
        mMediaController.setVideoPlayer(mBinding.mediaVideoPlayer);
        mBinding.mediaVideoPlayer.attachMediaController(mMediaController);
    }

    private void initListener() {

    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        resetAutoSizeConfig();
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            mBinding.layoutPlayer.setVisibility(View.GONE);
            mBinding.layoutExtra.setVisibility(View.VISIBLE);
        } else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mBinding.layoutPlayer.setVisibility(View.VISIBLE);
            mBinding.layoutExtra.setVisibility(View.GONE);
        }
    }

    public void resetAutoSizeConfig() {
        int width = ScreenUtils.getScreenSize(this)[0];
        int height = ScreenUtils.getScreenSize(this)[1];
        logger.i("reset auto size config [%s %s]", width, height);
        if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            //竖屏
            AutoSizeConfig.getInstance().setScreenWidth(Math.min(width, height));
            AutoSizeConfig.getInstance().setScreenHeight(Math.max(width, height));
            AutoSizeConfig.getInstance()
                    .setDesignWidthInDp(360)
                    .setDesignHeightInDp(640);
        } else {
            //横屏
            AutoSizeConfig.getInstance().setScreenWidth(Math.max(width, height));
            AutoSizeConfig.getInstance().setScreenHeight(Math.min(width, height));
            AutoSizeConfig.getInstance()
                    .setDesignWidthInDp(640)
                    .setDesignHeightInDp(360);
        }
    }

    @Override
    public void onGlobalLayout() {

    }

    public void clickPlayMovie(View view) {
        startPlayTask(PlayParam.toPlayTrailer("http://vjs.zencdn.net/v/oceans.mp4", true));
    }

    public void clickPlaySeries(View view) {
        startPlayTask(PlayParam.toPlayTrailer("http://vjs.zencdn.net/v/oceans.mp4", true));
    }

    public void clickPlayTrailer(View view) {
        startPlayTask(PlayParam.toPlayTrailer("http://vjs.zencdn.net/v/oceans.mp4", true));
    }

    private void startPlayTask(PlayParam param) {
        SdkServiceManager.getService(IPlay.class).setVideoView(mBinding.mediaVideoPlayer);
        SdkServiceManager.getService(IPlay.class).setMediaController(mMediaController);
        SdkServiceManager.getService(IPlay.class).startPlay(param).proceed(new Callback<Boolean>() {
            @Override
            public void onSuccess(Boolean aBoolean) {
                logger.i("onSuccess aBoolean: %s", aBoolean);
            }

            @Override
            public void onBusinessFail(int code, String message, Throwable throwable) {
                logger.e("onBusinessFail, code: %s, message: %s", code, message);
                throwable.printStackTrace();
                SdkServiceManager.getService(IPlay.class).stop();
            }

            @Override
            public void onError(Throwable throwable) {
                logger.e("start play error.");
                throwable.printStackTrace();
                SdkServiceManager.getService(IPlay.class).stop();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mMediaController != null) {
            mMediaController.onDestroy();
            mMediaController = null;
        }
        if (mWorkHandler != null) {
            mWorkHandler.removeCallbacksAndMessages(null);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            getWindow().getDecorView().getViewTreeObserver().removeOnGlobalLayoutListener(this);
        }
    }
}
