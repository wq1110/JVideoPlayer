package com.jw.media.jvideoplayer.player.youtube;

import android.text.TextUtils;

import com.jw.media.jvideoplayer.lib.log.Logger;
import com.jw.media.jvideoplayer.lib.log.LoggerFactory;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class DefaultYouTubeParser implements IYouTubeParser {
    private static Logger logger = LoggerFactory.getLogger(DefaultYouTubeParser.class.getSimpleName());

    static final String YOUTUBE_VIDEO_INFORMATION_URL = "https://www.youtube.com/get_video_info?html5=1&c=TVHTML5&cver=6.20180913&video_id=";
    static final String YOUTUBE_WEB_URL = "https://www.youtube.com/watch?v=";

    @Override
    public String getRealYouTubeUrl(String origUrl) throws Exception {
        logger.i("Default getRealYouTubeUrl is called.");

        String uriStr = null;
        String videoId = getYouTubeVideoId(origUrl);
        try {
            uriStr = getRealYouTubeUrl_20210729(videoId);
        } catch (Exception e) {
            e.printStackTrace();
            logger.e("get real youtube url 20210729 failed!!!");
        }
//        if (TextUtils.isEmpty(uriStr)) {
//            uriStr = getRealYouTubeUrl_Old(videoId);
//        }
        return uriStr;
    }

    /**
     * 解析规则，通过爬取网页数据获取播放地址，20210729更新
     *
     * @param videoId
     * @return
     */
    private String getRealYouTubeUrl_20210729(String videoId) throws Exception {
        String uriStr = null;
        int quality = 22;   // 17,18是中低质量;22,37中高质量

        String url = YOUTUBE_WEB_URL + videoId;
        logger.d("start timestamp:%d. url:%s", System.currentTimeMillis(), url);
        String infoStr = request(url);


//        String regex = "streamingData:\\s*\\[(.*?)\\]";
        String regex = "\"streamingData\"\\:\\{([^()])*\\}";
//        String regex = \"streamingData\"\:\{(.*?)\]\}
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(infoStr);
        while (m.find()) {
            String result = m.group();
            result = result.replaceFirst("\"streamingData\"\\:", "");
            JSONObject jsonObject = new JSONObject(result);
            JSONArray jsonArray = jsonObject.getJSONArray("formats");
            if (null == jsonArray || jsonArray.length() == 0) {
                jsonArray = jsonObject.getJSONArray("adaptiveFormats");
            }
            for (int i = 0; i < jsonArray.length(); i++) {
                int itag = jsonArray.getJSONObject(i).getInt("itag");
                String mimeType = jsonArray.getJSONObject(i).getString("mimeType");
                try{
                    uriStr = jsonArray.getJSONObject(i).getString("url");
                    if(TextUtils.isEmpty(uriStr)){
                        throw new RuntimeException("find signatureCipher!");
                    }
                }catch (Exception e){
                    String signatureCipher = jsonArray.getJSONObject(i).getString("signatureCipher");
                    uriStr = YoutubeHelper.parseCipherUrl(signatureCipher);
                }
                if (!TextUtils.isEmpty(mimeType) && mimeType.contains("video/mp4")) {
                    if (itag >= quality && !TextUtils.isEmpty(uriStr)) {
                        break;
                    }
                }
            }
        }
        String decodeUrl = null;
        if (uriStr != null) {
            decodeUrl = URLDecoder.decode(uriStr, "utf-8");
        }
        return decodeUrl;
    }

    /**
     * 解析规则，通过get_video_info接口获取播放地址，google限制，频繁请求会被禁止
     *
     * @return
     */
    private String getRealYouTubeUrl_Old(String videoId) throws Exception {
        String uriStr = null;
        final String quality = "22";   // 17,18是中低质量;22,37中高质量

        String url = YOUTUBE_VIDEO_INFORMATION_URL + videoId;
        logger.d("start timestamp:%d. url:%s", System.currentTimeMillis(), url);
        String infoStr = request(url);
        String[] args = infoStr.split("&");
        Map<String, String> argMap = new HashMap<String, String>();
        for (String arg : args) {
            String[] valStrArr = arg.split("=");
            if (valStrArr.length >= 2) {
                argMap.put(valStrArr[0], URLDecoder.decode(valStrArr[1]));
            }
        }
        String status = argMap.get("status");
        if (StringUtils.equalsIgnoreCase(status, "fail")) {
            String reason = argMap.get("reason");
            if (StringUtils.isEmpty(reason)) {
                throw new IOException("Can't get video resource data");
            } else {
                throw new IOException(reason);
            }
        }

        if (argMap.get("hlsvp") != null) {
            String liveStreamUrl = URLDecoder.decode(argMap.get("hlsvp"), "utf-8");
            return liveStreamUrl;
        }

        //Populate the list of formats for the video
        String fmt_list = argMap.get("fmt_list");
        if (!TextUtils.isEmpty(fmt_list)) {
            String fmtList = URLDecoder.decode(fmt_list, "utf-8");
            ArrayList<Format> formats = new ArrayList<Format>();
            if (null != fmtList) {
                String formatStrs[] = fmtList.split(",");

                for (String lFormatStr : formatStrs) {
                    Format format = new Format(lFormatStr);
                    formats.add(format);
                }
            }

            //Populate the list of streams for the video
            String streamList = argMap.get("url_encoded_fmt_stream_map");
            if (null != streamList) {
                String streamStrs[] = streamList.split(",");
                ArrayList<VideoStream> streams = new ArrayList<VideoStream>();
                for (String streamStr : streamStrs) {
                    VideoStream lStream = new VideoStream(streamStr);
                    streams.add(lStream);
                }

                //Search for the given format in the list of video formats
                // if it is there, select the corresponding stream
                // otherwise if fallback is requested, check for next lower format
                int formatId = Integer.parseInt(quality);

                Format searchFormat = new Format(formatId);
                while (!formats.contains(searchFormat)) {
                    int oldId = searchFormat.getId();
                    int newId = getSupportedFallbackId(oldId);

                    if (oldId == newId) {
                        break;
                    }
                    searchFormat = new Format(newId);
                }

                int index = formats.indexOf(searchFormat);
                if (index >= 0) {
                    VideoStream searchStream = streams.get(index);
                    uriStr = searchStream.getUrl();
                }

            }
        } else {
            String content = argMap.get("player_response");

            JSONObject jsonObject = new JSONObject(content);
//            JSONObject playabilityStatus = jsonObject.getJSONObject("playabilityStatus");
            JSONObject streamingData = jsonObject.getJSONObject("streamingData");
            if (null != streamingData) {
                JSONArray formats = streamingData.getJSONArray("formats");
                if (null == formats || formats.length() == 0) {
                    formats = streamingData.getJSONArray("adaptiveFormats");
                }

                if (null != formats && formats.length() > 0) {
                    for (int i = 0; i < formats.length(); i++) {
                        JSONObject format = formats.getJSONObject(i);
                        int itag = format.getInt("itag");
                        String mimeType = format.getString("mimeType");
                        if (!TextUtils.isEmpty(mimeType) && mimeType.contains("video/mp4")) {
                            uriStr = format.getString("url");
                            if (itag == 22 && !TextUtils.isEmpty(uriStr)) {
                                break;
                            }
                        }
                    }

                    if (TextUtils.isEmpty(uriStr)) {
                        uriStr = formats.getJSONObject(0).getString("url");
                    }
                }
            }
        }
        //Return the URI string. It may be null if the format (or a fallback format if enabled)
        // is not found in the list of formats for the video
        String decodeUrl = null;
        if (uriStr != null) {
            decodeUrl = URLDecoder.decode(uriStr, "utf-8");
        }
        return decodeUrl;
    }

    public static String decode(String unicodeStr) {
        if (unicodeStr == null) {
            return null;
        }
        StringBuffer retBuf = new StringBuffer();
        int maxLoop = unicodeStr.length();
        for (int i = 0; i < maxLoop; i++) {
            if (unicodeStr.charAt(i) == '\\') {
                if ((i < maxLoop - 5) && ((unicodeStr.charAt(i + 1) == 'u') || (unicodeStr.charAt(i + 1) == 'U')))
                    try {
                        retBuf.append((char) Integer.parseInt(unicodeStr.substring(i + 2, i + 6), 16));
                        i += 5;
                    } catch (NumberFormatException localNumberFormatException) {
                        retBuf.append(unicodeStr.charAt(i));
                    }
                else
                    retBuf.append(unicodeStr.charAt(i));
            } else {
                retBuf.append(unicodeStr.charAt(i));
            }
        }
        return retBuf.toString();
    }

    public static String getYouTubeVideoId(String youtubeUrl) {
        String videoId = "";
        if (youtubeUrl != null && youtubeUrl.trim().length() > 0
                && (StringUtils.startsWithIgnoreCase(youtubeUrl, "http") || StringUtils.startsWithIgnoreCase(youtubeUrl, "https"))) {
            String expression = "^.*((youtu.be\\/)|(v\\/)|(\\/u\\/w\\/)|(embed\\/)|(watch\\?))\\??v?=?([^#\\&\\?]*).*"; // var regExp = /^.*((youtu.be\/)|(v\/)|(\/u\/\w\/)|(embed\/)|(watch\?))\??v?=?([^#\&\?]*).*/;
            Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(youtubeUrl);
            if (matcher.matches()) {
                String groupIndex1 = matcher.group(7);
                if (groupIndex1 != null && groupIndex1.length() == 11)
                    videoId = groupIndex1;
            }
        }
        return videoId;
    }

    public static int getSupportedFallbackId(int oldId) {
        final int supportedFormatIds[] = {13,  //3GPP (MPEG-4 encoded) Low quality
                17,  //3GPP (MPEG-4 encoded) Medium quality
                18,  //MP4  (H.264 encoded) Normal quality
                22,  //MP4  (H.264 encoded) High quality
                37   //MP4  (H.264 encoded) High quality
        };
        int fallbackId = oldId;
        for (int i = supportedFormatIds.length - 1; i >= 0; i--) {
            if (oldId == supportedFormatIds[i] && i > 0) {
                fallbackId = supportedFormatIds[i - 1];
            }
        }
        return fallbackId;
    }
    /*@Override
    public String getRealYouTubeUrl(String origUrl) throws Exception {
        Timber.d("youtube: getRealYouTubeUrl is called.");
        final String quality = "22";   // 17,18是中低质量;22,37中高质量
        boolean fallback = true;
        String videoId = YouTubeData.getYouTubeVideoId(origUrl);

        String uriStr = null;
        OkHttpClient client = MyApplication.getInstance().getHttpClient();

        Request.Builder request = new Request.Builder();
        request.url(YouTubeData.YOUTUBE_VIDEO_INFORMATION_URL + videoId);
        Call call = client.newCall(request.build());
        Response response = call.execute();

        String infoStr = response.body().string();

        String[] args = infoStr.split("&");
        Map<String, String> argMap = new HashMap<String, String>();
        for (String arg : args) {
            String[] valStrArr = arg.split("=");
            if (valStrArr.length >= 2) {
                argMap.put(valStrArr[0], URLDecoder.decode(valStrArr[1]));
            }
        }
        String status = argMap.get("status");
        if(StringUtils.equalsIgnoreCase(status, "fail")){
            String reason = argMap.get("reason");
            if(StringUtils.isEmpty(reason)){
                throw new IOException("Can't get video resource data");
            }else{
                throw new IOException(reason);
            }
        }
        //Find out the URI string from the parameters

        //参考： http://stackoverflow.com/questions/35608686/how-can-i-get-the-actual-video-url-of-a-youtube-live-stream
        //check live stream url
        if(argMap.get("hlsvp") != null){
            String liveStreamUrl = URLDecoder.decode(argMap.get("hlsvp"), "utf-8");
            return liveStreamUrl;
        }
        //Populate the list of formats for the video
        String fmtList = URLDecoder.decode(argMap.get("fmt_list"), "utf-8");
        ArrayList<Format> formats = new ArrayList<Format>();
        if (null != fmtList) {
            String formatStrs[] = fmtList.split(",");

            for (String lFormatStr : formatStrs) {
                Format format = new Format(lFormatStr);
                formats.add(format);
            }
        }

        //Populate the list of streams for the video
        String streamList = argMap.get("url_encoded_fmt_stream_map");
        if (null != streamList) {
            String streamStrs[] = streamList.split(",");
            ArrayList<VideoStream> streams = new ArrayList<VideoStream>();
            for (String streamStr : streamStrs) {
                VideoStream lStream = new VideoStream(streamStr);
                streams.add(lStream);
            }

            //Search for the given format in the list of video formats
            // if it is there, select the corresponding stream
            // otherwise if fallback is requested, check for next lower format
            int formatId = Integer.parseInt(quality);

            Format searchFormat = new Format(formatId);
            while (!formats.contains(searchFormat) && fallback) {
                int oldId = searchFormat.getId();
                int newId = YouTubeData.getSupportedFallbackId(oldId);

                if (oldId == newId) {
                    break;
                }
                searchFormat = new Format(newId);
            }

            int index = formats.indexOf(searchFormat);
            if (index >= 0) {
                VideoStream searchStream = streams.get(index);
                uriStr = searchStream.getUrl();
            }

        }
        //Return the URI string. It may be null if the format (or a fallback format if enabled)
        // is not found in the list of formats for the video
        String decodeUrl = null;
        if(uriStr != null){
            decodeUrl = URLDecoder.decode(uriStr, "utf-8");
        }
        return decodeUrl;
    }*/

    private static String request(String url) throws IOException {
//        OkHttpClient client = new OkHttpClient.Builder().build();
//
//        Request.Builder request = new Request.Builder();
//        request.url(url);
//        Call call = client.newCall(request.build());
//        Response response = call.execute();
//
//        return response.body().string();

        try {
            HttpURLConnection connection = initHttps(url, "GET", null);
//            OutputStream out = connection.getOutputStream();
//            //out.write(params.getBytes(CHARSET_UTF8));
//            out.flush();
//            out.close();

            InputStream in = connection.getInputStream();
            BufferedReader read = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                read = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            }
            String valueString = null;
            StringBuffer bufferRes = new StringBuffer();
            while ((valueString = read.readLine()) != null) {
                bufferRes.append(valueString);
            }
            in.close();
            connection.disconnect();// 关闭连接

            return bufferRes.toString();
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    private static HttpsURLConnection initHttps(String url, String method, Map<String, String> headers)
            throws IOException, NoSuchAlgorithmException, NoSuchProviderException, KeyManagementException {
        TrustManager[] tm = {new CustomX509TrustManager()};
        SSLContext sslContext = SSLContext.getInstance("TLSv1");
        sslContext.init(null, tm, new java.security.SecureRandom());
        // 从上述SSLContext对象中得到SSLSocketFactory对象
        SSLSocketFactory ssf = sslContext.getSocketFactory();
        URL _url = new URL(url);
        HttpsURLConnection http = (HttpsURLConnection) _url.openConnection();
        // 连接超时
        http.setConnectTimeout(25000);
        // 读取超时 --服务器响应比较慢，增大时间
        http.setReadTimeout(25000);
        http.setRequestMethod(method);
        http.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        //注意：youtube有对浏览器版本做了限制，如果浏览器版本过低，可能导致拿不到streamingData数据，也就没法去解析实际的youtube播放地址
        http.setRequestProperty("User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36");
        if (null != headers && !headers.isEmpty()) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                http.setRequestProperty(entry.getKey(), entry.getValue());
            }
        }
        http.setSSLSocketFactory(ssf);
        http.setDoOutput(false);
        http.setDoInput(true);
        http.connect();
        return http;
    }

    static class CustomX509TrustManager implements X509TrustManager {

        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }
    }
}
