package com.jw.media.jvideoplayer.player.youtube.dynamicparse;

import android.text.TextUtils;

import com.google.gson.JsonSyntaxException;
import com.jw.media.jvideoplayer.lib.log.Logger;
import com.jw.media.jvideoplayer.lib.log.LoggerFactory;
import com.jw.media.jvideoplayer.lib.preferences.CommonPreference;
import com.jw.media.jvideoplayer.lib.utils.GsonUtil;
import com.jw.media.jvideoplayer.player.youtube.YoutubeHelper;
import com.jw.media.jvideoplayer.player.youtube.dynamicparse.config.IConfigJsonLoader;
import com.jw.media.jvideoplayer.player.youtube.dynamicparse.entity.CipherData;
import com.jw.media.jvideoplayer.player.youtube.dynamicparse.entity.SignSyntax;
import com.jw.media.jvideoplayer.player.youtube.dynamicparse.entity.UrlSyntax;

/**
 * 从后台服务获取配置文件
 */
public class ConfigParser extends AbstractCipherParser implements IConfigJsonLoader, IParserHandler {
    private static Logger logger = LoggerFactory.getLogger(ConfigParser.class.getSimpleName());

    private CipherData mCache;

    /**
     * 判断获取的json解析文件格式是否正确
     * 如果不正确直接返回null，交给下一个Parser去处理
     */
    private boolean isConfigValid;

    public static final String TRAILER_YOUTUBE_URL_PARSER_CONFIG = "trailer_youtube_url_parser_config";

    @Override
    String parseUrl(String url) {
        if (mCache == null) {
            String jsonString = loadJson();
            if (TextUtils.isEmpty(jsonString)) {
                return null;
            }
            try {
                mCache = GsonUtil.getGson().fromJson(jsonString, CipherData.class);
            } catch (JsonSyntaxException e) {
                logger.e("parse youtube config error: " + e.getMessage());
                return null;
            }

            isConfigValid = checkConfigValid(mCache);
        }

        if (isConfigValid) {
            logger.d("using config parser");
            return YoutubeHelper.parseConfigCipherUrl(url, this);
        }
        return null;
    }

    @Override
    public String loadJson() {
        return CommonPreference.getInstance().getString(TRAILER_YOUTUBE_URL_PARSER_CONFIG);
    }

    @Override
    public String parseRegex() {
        return mCache.getRegex();
    }

    @Override
    public SignSyntax parseSign() {
        return mCache.getSignSyntax();
    }

    @Override
    public UrlSyntax parseUrl() {
        return mCache.getUrlSyntax();
    }

    /**
     * String类型使用==null判断而非TextUtils.isEmpty
     * 以防止配置中的字段规则返回的的是""，所以只需要解析json有无相关属性就行
     */
    private boolean checkConfigValid(CipherData cipherData) {
        if (cipherData.getRegex() == null) {
            logger.e("parse regex error, using default regex");
            return false;
        }

        SignSyntax signSyntax = cipherData.getSignSyntax();
        if (signSyntax == null
                || signSyntax.getTarget() == null
                || signSyntax.getReplacement() == null) {
            logger.e("parse sign error, using default sign");
            return false;
        }

        UrlSyntax urlSyntax = cipherData.getUrlSyntax();

        if (urlSyntax == null
                || urlSyntax.getPrefix() == null
                || urlSyntax.getMethodInvokes() == null) {
            logger.e("parse url method error, using default url method");
            return false;
        }
        return true;
    }
}
