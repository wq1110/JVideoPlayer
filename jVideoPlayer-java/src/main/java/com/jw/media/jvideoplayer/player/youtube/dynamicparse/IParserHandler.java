package com.jw.media.jvideoplayer.player.youtube.dynamicparse;


import com.jw.media.jvideoplayer.player.youtube.dynamicparse.entity.SignSyntax;
import com.jw.media.jvideoplayer.player.youtube.dynamicparse.entity.UrlSyntax;

public interface IParserHandler {

    String parseRegex();

    SignSyntax parseSign();

    UrlSyntax parseUrl();
}
