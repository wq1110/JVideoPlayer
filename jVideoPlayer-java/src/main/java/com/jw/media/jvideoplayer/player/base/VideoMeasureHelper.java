package com.jw.media.jvideoplayer.player.base;

import android.view.View;

import com.jw.media.jvideoplayer.lib.log.Logger;
import com.jw.media.jvideoplayer.lib.log.LoggerFactory;
import com.jw.media.jvideoplayer.player.base.render.VideoSizeChangeListener;

import java.lang.ref.WeakReference;

/**
 * Created by Joyce.wang on 2024/3/11 10:23
 *
 * @Description Helper class to calculate the actual display width and height of a video.
 */
public class VideoMeasureHelper {
    private static Logger logger = LoggerFactory.getLogger(VideoMeasureHelper.class.getName());
    private WeakReference<View> mViewRef;

    private int mVideoWidth;
    private int mVideoHeight;
    private int mVideoSarNum;//横向采样数值
    private int mVideoSarDen;//纵向采样数值

    private int mVideoRotationDegree;//视频旋转角度

    private int mMeasuredWidth;//计算后的实际视频高宽
    private int mMeasuredHeight;//计算后的实际视频高度

    private int mCurrentAspectRatioMode = VideoType.AR_ASPECT_FIT_PARENT;
    private float mFullScreenExpansionPer = 1;//AR_ASPECT_FILL_PARENT模式宽高放大百分比
    private final VideoSizeChangeListener videoSizeChangeListener;

    public VideoMeasureHelper(View view, VideoSizeChangeListener listener) {
        videoSizeChangeListener = listener;
        mViewRef = new WeakReference<View>(view);
    }

    public View getView() {
        if (mViewRef == null)
            return null;
        return mViewRef.get();
    }

    public void setVideoSize(int videoWidth, int videoHeight) {
        mVideoWidth = videoWidth;
        mVideoHeight = videoHeight;
    }

    public void setVideoSampleAspectRatio(int videoSarNum, int videoSarDen) {
        mVideoSarNum = videoSarNum;
        mVideoSarDen = videoSarDen;
    }

    public void setVideoRotation(int videoRotationDegree) {
        mVideoRotationDegree = videoRotationDegree;
    }

    public void setFullScreenWidthExpansionPer(float fullScreenWidthExpansionPer) {
        mFullScreenExpansionPer = fullScreenWidthExpansionPer;
    }

    public void setAspectRatioMode(int aspectRatioMode) {
        mCurrentAspectRatioMode = aspectRatioMode;
    }

    /**
     * Must be called by View.onMeasure(int, int)
     *
     * @param widthMeasureSpec  width measure spec
     * @param heightMeasureSpec height measure spec
     */
    public void doMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mVideoRotationDegree == 90 || mVideoRotationDegree == 270) {
            int tempSpec = widthMeasureSpec;
            widthMeasureSpec = heightMeasureSpec;
            heightMeasureSpec = tempSpec;
        }

        int realWidth = mVideoWidth;

//        if(mVideoSarNum != 0 && mVideoSarDen != 0) {
//            double pixelWidthHeightRatio = mVideoSarNum / (mVideoSarDen / 1.0);
//            realWidth = (int) (pixelWidthHeightRatio * mVideoWidth);
//        }

        int width = View.getDefaultSize(realWidth, widthMeasureSpec);
        int height = View.getDefaultSize(mVideoHeight, heightMeasureSpec);
        logger.i("doMeasure currentAspectRatioMode[%s], [%s, %s] [%s, %s] [%s, %s]", mCurrentAspectRatioMode, realWidth, mVideoHeight, width, height, mVideoSarNum, mVideoSarDen);
        if (mCurrentAspectRatioMode == VideoType.AR_MATCH_PARENT) {
            width = widthMeasureSpec;
            height = heightMeasureSpec;
        } else if (realWidth > 0 && mVideoHeight > 0) {
            int widthSpecMode = View.MeasureSpec.getMode(widthMeasureSpec);
            int widthSpecSize = View.MeasureSpec.getSize(widthMeasureSpec);
            int heightSpecMode = View.MeasureSpec.getMode(heightMeasureSpec);
            int heightSpecSize = View.MeasureSpec.getSize(heightMeasureSpec);
            logger.i("doMeasure widthSpecSize: %s, heightSpecSize: %s", widthSpecSize, heightSpecSize);

            if (widthSpecMode == View.MeasureSpec.AT_MOST && heightSpecMode == View.MeasureSpec.AT_MOST) {
                float specAspectRatio = (float) widthSpecSize / (float) heightSpecSize;
                float displayAspectRatio;
                switch (mCurrentAspectRatioMode) {
                    case VideoType.AR_16_9_FIT_PARENT:
                        displayAspectRatio = 16.0f / 9.0f;
                        if (mVideoRotationDegree == 90 || mVideoRotationDegree == 270)
                            displayAspectRatio = 1.0f / displayAspectRatio;
                        break;
                    case VideoType.AR_4_3_FIT_PARENT:
                        displayAspectRatio = 4.0f / 3.0f;
                        if (mVideoRotationDegree == 90 || mVideoRotationDegree == 270)
                            displayAspectRatio = 1.0f / displayAspectRatio;
                        break;
                    case VideoType.AR_ASPECT_FIT_PARENT:
                    case VideoType.AR_ASPECT_FILL_PARENT:
                    case VideoType.AR_ASPECT_WRAP_CONTENT:
                    default:
                        displayAspectRatio = (float) realWidth / (float) mVideoHeight;
                        if (mVideoSarNum > 0 && mVideoSarDen > 0)
                            displayAspectRatio = displayAspectRatio * mVideoSarNum / mVideoSarDen;
                        break;
                }
                boolean shouldBeWider = displayAspectRatio > specAspectRatio;
                logger.i("doMeasure shouldBeWider[%s] displayAspectRatio[%s] specAspectRatio[%s] fullScreenExpansionPer[%s]",
                        shouldBeWider, displayAspectRatio, specAspectRatio, mFullScreenExpansionPer);

                switch (mCurrentAspectRatioMode) {
                    case VideoType.AR_ASPECT_FIT_PARENT:
                    case VideoType.AR_16_9_FIT_PARENT:
                    case VideoType.AR_4_3_FIT_PARENT:
                        if (shouldBeWider) {
                            // too wide, fix width
                            width = widthSpecSize;
                            height = (int) (width / displayAspectRatio);
                        } else {
                            // too high, fix height
                            height = heightSpecSize;
                            width = (int) (height * displayAspectRatio);
                        }
                        break;
                    case VideoType.AR_ASPECT_FILL_PARENT:
                        //针对视频宽高比和显示view宽高比不一致，以及有些视频本来就存在黑边问题，导致播放中没法针对所有的影片做全屏播放。
                        // 这里先这样处理（会导致上下左右画面有裁剪，显示不全），后续有问题再想其他方法解决。
                        if (shouldBeWider) {
                            // not high enough, fix height
                            height = (int) (heightSpecSize * mFullScreenExpansionPer);
                            width = (int) (height * displayAspectRatio);
                        } else {
                            // not wide enough, fix width
                            width = (int) (widthSpecSize * mFullScreenExpansionPer);
                            height = (int) (width / displayAspectRatio);
                        }
                        break;
                    case VideoType.AR_ASPECT_WRAP_CONTENT:
                    default:
                        if (shouldBeWider) {
                            // too wide, fix width
                            width = Math.min(realWidth, widthSpecSize);
                            height = (int) (width / displayAspectRatio);
                        } else {
                            // too high, fix height
                            height = Math.min(mVideoHeight, heightSpecSize);
                            width = (int) (height * displayAspectRatio);
                        }
                        break;
                }
            } else if (widthSpecMode == View.MeasureSpec.EXACTLY && heightSpecMode == View.MeasureSpec.EXACTLY) {
                // the size is fixed
                width = widthSpecSize;
                height = heightSpecSize;

                // for compatibility, we adjust size based on aspect ratio
                if (realWidth * height < width * mVideoHeight) {
                    //Log.i("@@@", "image too wide, correcting");
                    width = height * realWidth / mVideoHeight;
                } else if (realWidth * height > width * mVideoHeight) {
                    //Log.i("@@@", "image too tall, correcting");
                    height = width * mVideoHeight / realWidth;
                }
            } else if (widthSpecMode == View.MeasureSpec.EXACTLY) {
                // only the width is fixed, adjust the height to match aspect ratio if possible
                width = widthSpecSize;
                height = width * mVideoHeight / realWidth;
                if (heightSpecMode == View.MeasureSpec.AT_MOST && height > heightSpecSize) {
                    // couldn't match aspect ratio within the constraints
                    height = heightSpecSize;
                }
            } else if (heightSpecMode == View.MeasureSpec.EXACTLY) {
                // only the height is fixed, adjust the width to match aspect ratio if possible
                height = heightSpecSize;
                width = height * realWidth / mVideoHeight;
                if (widthSpecMode == View.MeasureSpec.AT_MOST && width > widthSpecSize) {
                    // couldn't match aspect ratio within the constraints
                    width = widthSpecSize;
                }
            } else {
                // neither the width nor the height are fixed, try to use actual video size
                width = realWidth;
                height = mVideoHeight;
                if (heightSpecMode == View.MeasureSpec.AT_MOST && height > heightSpecSize) {
                    // too tall, decrease both width and height
                    height = heightSpecSize;
                    width = height * realWidth / mVideoHeight;
                }
                if (widthSpecMode == View.MeasureSpec.AT_MOST && width > widthSpecSize) {
                    // too wide, decrease both width and height
                    width = widthSpecSize;
                    height = width * mVideoHeight / realWidth;
                }
            }
        } else {
            // no size yet, just adopt the given spec sizes
        }
        logger.i("doMeasure, final measure size[%s, %s]", width, height);

        mMeasuredWidth = width;
        mMeasuredHeight = height;
    }

    public void prepareMeasure(int widthMeasureSpec, int heightMeasureSpec, int rotate) {
        if (videoSizeChangeListener != null) {
            try {
                int videoWidth = videoSizeChangeListener.getCurrentVideoWidth();
                int videoHeight = videoSizeChangeListener.getCurrentVideoHeight();
                int videoSarNum = videoSizeChangeListener.getVideoSarNum();
                int videoSarDen = videoSizeChangeListener.getVideoSarDen();
                int aspectRatioMode = videoSizeChangeListener.getAspectRatioMode();
                logger.i("videoWidth: %s, videoHeight: %s, videoSarNum: %s, videoSarDen: %s", videoWidth, videoHeight, videoSarNum, videoSarDen);

                if (videoSarNum > 0 && videoSarDen > 0) {
                    setVideoSampleAspectRatio(videoSarNum, videoSarDen);
                }
                if (videoWidth > 0 && videoHeight > 0) {
                    setVideoSize(videoWidth, videoHeight);
                }
                if (aspectRatioMode >= 0) {
                    setAspectRatioMode(aspectRatioMode);
                }
                setVideoRotation(rotate);
                doMeasure(widthMeasureSpec, heightMeasureSpec);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public int getMeasuredWidth() {
        return mMeasuredWidth;
    }

    public int getMeasuredHeight() {
        return mMeasuredHeight;
    }
}
