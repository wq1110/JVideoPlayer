package com.jw.media.jvideoplayer.player.youtube.dynamicparse;

import android.text.TextUtils;

import com.jw.media.jvideoplayer.lib.log.Logger;
import com.jw.media.jvideoplayer.lib.log.LoggerFactory;
import com.jw.media.jvideoplayer.player.youtube.HttpUtil;

import java.net.HttpURLConnection;

public abstract class AbstractCipherParser implements ICipherParser {
    private static Logger logger = LoggerFactory.getLogger(AbstractCipherParser.class.getSimpleName());
    private ICipherParser nextParser;

    abstract String parseUrl(String url);

    @Override
    public void setNext(ICipherParser parser) {
        this.nextParser = parser;
    }

    @Override
    public String parse(String url) {
        String parsedUrl = parseUrl(url);
        if (checkUrlValid(parsedUrl)) {
            return url;
        }
        if (nextParser != null) {
            logger.d("url no valid, using nextParser: " + nextParser.getClass().getSimpleName());
            return nextParser.parse(url);
        }
        logger.e("parse failure, url is null");
        return null;
    }

    private boolean checkUrlValid(String url) {
        if (TextUtils.isEmpty(url)) {
            return false;
        }

        try {
            HttpURLConnection connection = HttpUtil.initHttps(url, "GET", null);
            return connection.getResponseCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }
}
