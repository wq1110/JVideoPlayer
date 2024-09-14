package com.jw.media.jvideoplayer.player.youtube;

import android.text.TextUtils;

import com.jw.media.jvideoplayer.lib.log.Logger;
import com.jw.media.jvideoplayer.lib.log.LoggerFactory;
import com.jw.media.jvideoplayer.player.youtube.callback.BiFunction;
import com.jw.media.jvideoplayer.player.youtube.dynamicparse.ConfigParser;
import com.jw.media.jvideoplayer.player.youtube.dynamicparse.GvAgainParser;
import com.jw.media.jvideoplayer.player.youtube.dynamicparse.GvParser;
import com.jw.media.jvideoplayer.player.youtube.dynamicparse.IParserHandler;
import com.jw.media.jvideoplayer.player.youtube.dynamicparse.entity.MethodValue;
import com.jw.media.jvideoplayer.player.youtube.dynamicparse.entity.SignSyntax;
import com.jw.media.jvideoplayer.player.youtube.dynamicparse.entity.UrlSyntax;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class YoutubeHelper {
    private static Logger logger = LoggerFactory.getLogger(YoutubeHelper.class.getSimpleName());

    static String[] formatKey = {"%20", "%22", "%23", "%25", "%26", "%28", "%29", "%2B", "%2C", "%2F", "%3A", "%3B", "%3C", "%3D", "%3E", "%3F", "%40", "%5C", "%7C"};
    static String[] formatValue = {" ", "\"", "#", "%", "&", "(", ")", "+", ",", "/", ":", ";", "<", "=", ">", "?", "@", "\\", "|"};

    public static String getSignatureCipher(String s) {
        try {
            return URLDecoder.decode(s, "utf-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String formatStr(String s) {
        String newCryUrl = "";
        for (int i = 0; i < formatKey.length; i++) {
            newCryUrl = s.replaceAll(formatKey[i], formatValue[i]);
        }
        return newCryUrl;
    }

    public static String parseCipherUrl(String originalUrl) {
        ConfigParser configParser = new ConfigParser();
        GvParser gvParser = new GvParser();
        GvAgainParser gvAgainParser = new GvAgainParser();
        //组装责任链
        configParser.setNext(gvParser);
        gvParser.setNext(gvAgainParser);
        //开始解析
        return configParser.parse(originalUrl);
    }

//    public static String parseCipherUrl(String orignalUrl, boolean again) {
//        String[] split = getSignatureCipher(orignalUrl).split("&sp=sig&url=");
//        String cryUrl = split[1];
//        String sign = split[0].replace("s=", "");
//
//        String signatureUrl = formatStr(cryUrl) + "&sig=" + (again ? GvAgain(sign) : Gv(sign));
//        return signatureUrl;
//    }

    public static String parseConfigCipherUrl(String originalUrl, IParserHandler handler) {
        String regex = handler.parseRegex();
        String[] split = getSignatureCipher(originalUrl).split(regex);
        String cryUrl = split[1];
        SignSyntax signSyntax = handler.parseSign();
        String sign = split[0].replace(signSyntax.getTarget(), signSyntax.getReplacement());

        UrlSyntax urlSyntax = handler.parseUrl();
        return formatStr(cryUrl) + urlSyntax.getPrefix() + cipherParse(sign, urlSyntax);
    }

//    public static String GvAgain(String sign) {
//        String s1 = KQ(sign, 49);
//        String s2 = KQ(s1, 20);
//        String s3 = KQ(s2, 25);
//        return d3(s3, 1);
//    }
//
//    public static String Gv(String sign) {
//        String s1 = gT(sign, 71);
//        String s2 = KQ(s1, 19);
//        String s3 = KQ(s2, 22);
//        String s4 = KQ(s3, 66);
//        String s5 = gT(s4, 79);
//        return KQ(s5, 36);
//    }

    private static String cipherParse(String sign, UrlSyntax urlSyntax) {
        BiFunction<String, Integer, String> gtInvoke = YoutubeHelper::gT;
        BiFunction<String, Integer, String> KQInvoke = YoutubeHelper::KQ;
        BiFunction<String, Integer, String> d3Invoke = YoutubeHelper::d3;
        String result = sign;
        for (MethodValue methodValue : urlSyntax.getMethodInvokes()) {
            String methodName = methodValue.getMethodName();
            int value = methodValue.getValue();
            switch (methodName) {
                case "gT":
                    logger.d("cipherParse method: gT, value: " + value);
                    result = gtInvoke.apply(result, value);
                    break;
                case "KQ":
                    logger.d("cipherParse method: KQ, value: " + value);
                    result = KQInvoke.apply(result, value);
                    break;
                case "d3":
                    logger.d("cipherParse method: d3, value: " + value);
                    result = d3Invoke.apply(result, value);
                    break;
                default:
                    logger.e("no such method: " + methodName);
                    break;
            }
        }
        return result;
    }

    //{var c=a[0];a[0]=a[b%a.length];a[b%a.length]=c}
    public static String KQ(String aStr, int b) {
        if (TextUtils.isEmpty(aStr)) {
            return "";
        }
        char[] a = aStr.toCharArray();
        char c = a[0];
        a[0] = a[b % a.length];
        a[b % a.length] = c;
        return new String(a);
    }

    //a.spilt(0,b)
    public static String d3(String aStr, int b) {
        String newStr = aStr.substring(b);
        return newStr;
    }

    public static String gT(String aStr, int b) {
        String newStr = new StringBuilder().append(aStr).reverse().toString();
        return newStr;
    }
}
