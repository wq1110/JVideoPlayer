package com.jw.media.jvideoplayer.player.youtube.dynamicparse;

import com.jw.media.jvideoplayer.player.youtube.dynamicparse.entity.SignSyntax;

/**
 * 目前gv和gvAgain相同的部分从这边获取
 * 如果后续改动每个Parser中解析规则都不一样的话，可以将这个删除，分别在Parser中独立实现
 */
public class DefaultParserProvider {
    private static final String DEFAULT_REGEX = "&sp=sig&url=";
    private static SignSyntax sDefaultSignSyntax;
    private static final String DEFAULT_URL_SYNTAX_PREFIX = "&sig=";

    public static String provideDefaultRegex() {
        return DEFAULT_REGEX;
    }

    public static SignSyntax provideDefaultSignSyntax() {
        if (sDefaultSignSyntax == null) {
            sDefaultSignSyntax = new SignSyntax();

            // add sign default
            // String sign = split[0].replace("s=", "");
            sDefaultSignSyntax.setTarget("s=");
            sDefaultSignSyntax.setReplacement("");
        }

        return sDefaultSignSyntax;
    }

    public static String providerDefaultPrefix() {
        return DEFAULT_URL_SYNTAX_PREFIX;
    }
}
