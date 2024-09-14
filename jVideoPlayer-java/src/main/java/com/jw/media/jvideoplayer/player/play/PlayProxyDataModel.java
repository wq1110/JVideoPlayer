package com.jw.media.jvideoplayer.player.play;

import androidx.annotation.NonNull;

import com.jw.media.jvideoplayer.lib.log.Logger;
import com.jw.media.jvideoplayer.lib.log.LoggerFactory;

/**
 * Created by Joyce.wang on 2024/9/13 10:32
 *
 * @Description TODO
 */
public class PlayProxyDataModel extends ItemDataModel<PlayRepository> {
    private static Logger logger = LoggerFactory.getLogger(PlayProxyDataModel.class.getSimpleName());

    public PlayProxyDataModel(@NonNull PlayRepository dataModel) {
        super(dataModel);
        initData();
    }

    private void initData() {

    }

    public void clean() {

    }
}
