package com.jw.media.jvideoplayer.player.youtube.dynamicparse;

/**
 * 使用责任链模式传递
 */
public interface ICipherParser {
    void setNext(ICipherParser parser);

    String parse(String url);
}
