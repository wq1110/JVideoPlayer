package com.jw.media.jvideoplayer.player.controller;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Message;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.jw.media.jvideoplayer.java.R;
import com.jw.media.jvideoplayer.lib.ass.AssView;
import com.jw.media.jvideoplayer.lib.log.Logger;
import com.jw.media.jvideoplayer.lib.log.LoggerFactory;
import com.jw.media.jvideoplayer.player.play.PlayConstant;

/**
 * Created by Joyce.wang on 2024/3/12 19:36
 *
 * @Description Subtitle processing center during media playback
 */
public abstract class MediaSubController extends MediaNetworkController {
    private static Logger logger = LoggerFactory.getLogger(MediaSubController.class.getSimpleName());

    public static final String KEY_SUBTITLE_TEXT = "key_subtitle_text";
    public static final String KEY_SUBTITLE_TEXT_SIZE = "key_subtitle_text_size";
    public static final String KEY_SUBTITLE_TEXT_COLOR = "key_subtitle_text_color";
    public static final String KEY_SUBTITLE_TEXT_BACKGROUND = "key_subtitle_text_background";
    public static final String KEY_IS_SHOW_SUBTITLE_VIEW = "key_is_show_subtitle_view";
    public static final String KEY_SUBTITLE_STAT = "key_subtitle_stat";

    public enum SubtitleUpdateType {
        TEXT, TEXT_SIZE, TEXT_COLOR, TEXT_BACKGROUND_COLOR
    }

    protected TextView mMediaSubtitleView;//srt格式字幕view
    protected AssView mMediaAssView;//ass格式字幕view
    protected LinearLayout mMediaSubtitle;//切换字幕view
    protected ImageView mMediaSubtitleIv;
    protected TextView mMediaSubtitleTv;

    public MediaSubController(@NonNull Context context) {
        super(context);
    }

    public MediaSubController(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MediaSubController(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MediaSubController(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void init(Context context) {
        super.init(context);
        //subtitles box
        mMediaSubtitleView = findViewById(R.id.media_subtitle_view);
        mMediaSubtitle = findViewById(R.id.media_subtitle);
        mMediaSubtitleIv = findViewById(R.id.media_subtitle_iv);
        mMediaSubtitleTv = findViewById(R.id.media_subtitle_tv);
        mMediaAssView = findViewById(R.id.media_ass_view);

        mMediaSubtitle.setOnClickListener(this);

        initListener();
    }

    private void initListener() {
        mMediaSubtitle.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    mMediaSubtitle.setSelected(true);
                } else {
                    mMediaSubtitle.setSelected(false);
                }
            }
        });
    }

    @Override
    protected void onHandleMessage(Message msg) {
        super.onHandleMessage(msg);
        if (msg.what == PlayConstant.Message.MESSAGE_UPDATE_SUBTITLE) {
            Bundle bundle = msg.getData();
            if (bundle != null) {
                SubtitleUpdateType updateType = SubtitleUpdateType.values()[msg.arg1];
                switch (updateType) {
                    case TEXT:
                        String subtitleText = bundle.getString(KEY_SUBTITLE_TEXT);
                        boolean isShowSubtitleView = bundle.getBoolean(KEY_IS_SHOW_SUBTITLE_VIEW);
                        updateSubtitleText(isShowSubtitleView, subtitleText);
                        break;
                    case TEXT_SIZE:
                        float subtitleSize = bundle.getFloat(KEY_SUBTITLE_TEXT_SIZE);
                        mMediaSubtitleView.setTextSize(subtitleSize);
                        break;
                    case TEXT_COLOR:
                        int subtitleColor = bundle.getInt(KEY_SUBTITLE_TEXT_COLOR);
                        mMediaSubtitleView.setTextColor(subtitleColor);
                        break;
                    case TEXT_BACKGROUND_COLOR:
                        int subtitleBackground = bundle.getInt(KEY_SUBTITLE_TEXT_BACKGROUND);
                        mMediaSubtitleView.setBackgroundColor(subtitleBackground);
                        break;
                }
            }
        }
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        int id = v.getId();
        if (R.id.media_subtitle == id) {
            switchSubtitles();
        }
    }

    public void switchSubtitles() {
        if (mOnMediaControllerListener != null) {
            mOnMediaControllerListener.showSubtitleDialog(((FragmentActivity)mContext).getSupportFragmentManager());
        }
    }

    //隐藏字幕
    public void hideSubtitles() {
        this.mMediaSubtitle.setVisibility(GONE);
    }

    //展示字幕
    public void showSubtitles() {
        this.mMediaSubtitle.setVisibility(VISIBLE);
    }

    //更新字幕信息
    public void updateSubtitleText(boolean subtitleInShifting, Spanned spannedText) {
        //暂停缓冲时的播放进度会出现不稳定情况，导致字幕忽前忽后，所以在缓冲时停止字幕显示
        Bundle bundle = new Bundle();
        bundle.putString(KEY_SUBTITLE_TEXT, spannedText != null ? spannedText.toString() : "");
        Message message = mHandler.obtainMessage(PlayConstant.Message.MESSAGE_UPDATE_SUBTITLE);
        message.arg1 = SubtitleUpdateType.TEXT.ordinal();
        if (!isPlaying() && !subtitleInShifting) {
            bundle.putBoolean(KEY_IS_SHOW_SUBTITLE_VIEW, false);
            message.setData(bundle);
            mHandler.sendMessage(message);
        } else {
            bundle.putBoolean(KEY_IS_SHOW_SUBTITLE_VIEW, true);
            message.setData(bundle);
            mHandler.sendMessage(message);
        }
    }

    private void updateSubtitleText(boolean isShowSubtitleView, String subtitleTxt) {
        if (!isShowSubtitleView) {
            if (mMediaSubtitleView.getVisibility() == VISIBLE) {
                mMediaSubtitleView.setVisibility(GONE);
            }
        } else {
            if (mMediaSubtitleView.getVisibility() != VISIBLE) {
                mMediaSubtitleView.setVisibility(VISIBLE);
            }
        }
        if (mMediaSubtitleView.getVisibility() != VISIBLE) {
            return;
        }

        if (!TextUtils.isEmpty(subtitleTxt)) {
            setSubtitleText(subtitleTxt);
        } else {
            setSubtitleText("");
        }
    }
    protected void setSubtitleText(String subText) {
        mMediaSubtitleView.setText(subText);
    }

    public void toggleSubtitleView(boolean show) {
        this.mMediaSubtitleView.setVisibility(show ? VISIBLE : GONE);
    }

    public AssView getMediaAssView() {
        return this.mMediaAssView;
    }

    //设置字幕大小
    protected float mCurrentSubtitleTextSize = 1;
    public void setSubtitleTextSize(float size) {
        mCurrentSubtitleTextSize = size;
        sendSubtitleUpdateMessage(SubtitleUpdateType.TEXT_SIZE, KEY_SUBTITLE_TEXT_SIZE, size);
    }

    //设置字幕颜色
    public void setSubtitleTextColor(int textColor) {
        sendSubtitleUpdateMessage(SubtitleUpdateType.TEXT_COLOR, KEY_SUBTITLE_TEXT_COLOR, textColor);
    }

    //设置字幕背景颜色
    public void setSubtitleBackgroundColor(int backColor) {
        sendSubtitleUpdateMessage(SubtitleUpdateType.TEXT_BACKGROUND_COLOR, KEY_SUBTITLE_TEXT_BACKGROUND, backColor);
    }

    // Helper method to send subtitle update messages
    private void sendSubtitleUpdateMessage(SubtitleUpdateType updateType, String key, Object value) {
        Bundle bundle = new Bundle();
        if (value instanceof Float) {
            bundle.putFloat(key, (Float) value);
        } else if (value instanceof Integer) {
            bundle.putInt(key, (Integer) value);
        }
        Message message = mHandler.obtainMessage(PlayConstant.Message.MESSAGE_UPDATE_SUBTITLE);
        message.arg1 = updateType.ordinal();
        message.setData(bundle);
        mHandler.sendMessage(message);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
