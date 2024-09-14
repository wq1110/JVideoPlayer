package com.jw.media.jvideoplayer.player.play;

import androidx.annotation.NonNull;

/**
 * Created by Joyce.wang on 2024/9/13 10:32
 *
 * @Description TODO
 */
public class ItemDataModel<VM extends BaseDataModel> {
    protected VM mDataModel;
    public ItemDataModel(@NonNull VM dataModel) {
        this.mDataModel = dataModel;
    }
}