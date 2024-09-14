package com.jw.media.jvideoplayer.mvx.mvp;

import java.util.HashMap;

/**
 * Created by Joyce.wang on 2024/9/13 14:34
 *
 * @Description TODO
 */
public interface MVPInfoFiller {
    // 对应关系  <ViewInterface, PrensenterClass>
    void fill(HashMap<Class<? extends BaseViewInterface>, Class<? extends BasePresenter>> map);
}
