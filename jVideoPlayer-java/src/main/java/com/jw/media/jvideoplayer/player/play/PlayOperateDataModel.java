package com.jw.media.jvideoplayer.player.play;

import android.app.Activity;

import androidx.annotation.NonNull;

import com.jw.media.jvideoplayer.java.R;
import com.jw.media.jvideoplayer.lib.log.Logger;
import com.jw.media.jvideoplayer.lib.log.LoggerFactory;
import com.jw.media.jvideoplayer.lib.provider.ContextProvider;
import com.jw.media.jvideoplayer.lib.utils.NetworkUtils;
import com.jw.media.jvideoplayer.mvx.mvp.APIDataListener;

/**
 * Created by Joyce.wang on 2024/9/13 10:58
 *
 * @Description TODO
 */
public class PlayOperateDataModel extends ItemDataModel<PlayRepository> {
    private static Logger logger = LoggerFactory.getLogger(PlayOperateDataModel.class.getSimpleName());

    public PlayOperateDataModel(@NonNull PlayRepository dataModel) {
        super(dataModel);
    }

    public void showNetworkChooseDialog(Activity activity, APIDataListener<Boolean> listener) {
        if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
            return;
        }
        if (!NetworkUtils.isConnectingToInternet()) {
            if (listener != null) {
                listener.onError(new RuntimeException(ContextProvider.getContext().getString(R.string.sdk_vod_c_play_str_network_is_not_available)));
            }
            return;
        }
        //TODO: Add network type handling
        if (listener != null) listener.onSuccess(true);
    }
}
