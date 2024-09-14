package com.jw.media.jvideoplayer.lib.ass;

import android.content.Context;

public interface IAssView {
    void render(AssImage[] assImage);
    int getWidth();
    int getHeight();
    public Context getContext();
}
