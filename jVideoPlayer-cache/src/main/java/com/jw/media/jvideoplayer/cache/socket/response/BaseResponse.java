package com.jw.media.jvideoplayer.cache.socket.response;

import android.text.TextUtils;

import com.jw.media.jvideoplayer.cache.VideoProxyCacheManager;
import com.jw.media.jvideoplayer.cache.common.VideoCacheException;
import com.jw.media.jvideoplayer.cache.socket.request.ChunkedOutputStream;
import com.jw.media.jvideoplayer.cache.socket.request.ContentType;
import com.jw.media.jvideoplayer.cache.socket.request.HttpRequest;
import com.jw.media.jvideoplayer.cache.socket.request.IState;
import com.jw.media.jvideoplayer.cache.socket.request.Method;
import com.jw.media.jvideoplayer.cache.socket.request.ResponseState;
import com.jw.media.jvideoplayer.cache.utils.ProxyCacheUtils;
import com.jw.media.jvideoplayer.lib.log.Logger;
import com.jw.media.jvideoplayer.lib.log.LoggerFactory;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
/**
 * Created by Joyce.wang on 2024/9/11 8:58
 *
 * @Description TODO
 */
public abstract class BaseResponse {
    private static Logger logger = LoggerFactory.getLogger(BaseResponse.class.getName());
    protected static String CONTENT_TYPE = "Content-Type";
    protected static String CONTENT_LENGTH = "Content-Length";
    protected static String CONTENT_RANGE = "Content-Range";
    protected static String ACCEPT_RANGES = "Accept-Ranges";
    protected static String DATE = "Date";
    protected static String CONNECTION = "Connection";
    protected static String TRANSFER_ENCODING = "Transfer-Encoding";
    protected static String GMT_PATTERN = "E, d MMM yyyy HH:mm:ss 'GMT'";
    protected static final int WAIT_TIME = 50;
    protected static final int MAX_WAIT_TIME = 2 * 1000;

    protected final HttpRequest mRequest;
    protected final String mCachePath;
    protected final String mVideoUrl;
    protected final long mCurrentTime;
    protected Map<String, String> mHeaders;
    protected final String mMimeType;
    protected final String mProtocolVersion;
    protected IState mResponseState;
    protected long mTotalSize;
    protected long mStartPosition;

    public BaseResponse(HttpRequest request, String videoUrl, Map<String, String> headers, long time) {
        mRequest = request;
        mCachePath = ProxyCacheUtils.getConfig().getFilePath();
        mVideoUrl = videoUrl;
        mHeaders = headers;
        mCurrentTime = time;
        mMimeType = request.getMimeType();
        mProtocolVersion = request.getProtocolVersion();
    }

    public void sendResponse(Socket socket, OutputStream outputStream) throws VideoCacheException {
        SimpleDateFormat gmtFormat = new SimpleDateFormat(GMT_PATTERN, Locale.US);
        gmtFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        try {
            if (mResponseState == null) {
                throw new VideoCacheException("sendResponse(): Status can't be null.");
            }
            PrintWriter pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(outputStream, new ContentType(mMimeType).getEncoding())),false);
            if (TextUtils.isEmpty(mProtocolVersion)) {
                pw.append("HTTP/1.1 ");
            } else {
                pw.append(mProtocolVersion).append(" ");
            }
            pw.append(mResponseState.getDescription()).append(" \r\n");
            if (!TextUtils.isEmpty(mMimeType)) {
                appendHeader(pw, CONTENT_TYPE, mMimeType);
            }
            appendHeader(pw, DATE, gmtFormat.format(new Date()));
            appendHeader(pw, CONNECTION, (mRequest.keepAlive() ? "keep-alive" : "close"));
            if (mRequest.requestMethod() != Method.HEAD) {
                appendHeader(pw, TRANSFER_ENCODING, "chunked");
            }
            if (mResponseState == ResponseState.PARTIAL_CONTENT) {
                long contentLength = mTotalSize - mStartPosition + 1;
                appendHeader(pw, CONTENT_LENGTH, String.valueOf(contentLength));

                String contentRange = String.format("bytes %s-%s/%s", String.valueOf(mStartPosition), String.valueOf(mTotalSize), String.valueOf(mTotalSize));
                appendHeader(pw, CONTENT_RANGE, contentRange);
            }
            pw.append("\r\n");
            pw.flush();
            sendBodyWithCorrectTransferAndEncoding(socket, outputStream);
            outputStream.flush();
        } catch (Exception e) {
            throw new VideoCacheException("send response failed: ", e);
        }
    }

    protected void appendHeader(PrintWriter pw, String key, String value) {
        pw.append(key).append(": ").append(value).append("\r\n");
    }

    protected void sendBodyWithCorrectTransferAndEncoding(Socket socket, OutputStream outputStream) throws Exception {
        ChunkedOutputStream chunkedOutputStream = new ChunkedOutputStream(outputStream);
        sendBody(socket, chunkedOutputStream, -1);
        chunkedOutputStream.finish();
    }

    public abstract void sendBody(Socket socket, OutputStream outputStream, long pending) throws Exception;

    protected boolean shouldSendResponse(Socket socket, String md5) {
        return !socket.isClosed() && TextUtils.equals(md5, VideoProxyCacheManager.getInstance().getPlayingUrlMd5()) && (mCurrentTime == ProxyCacheUtils.getSocketTime());
    }

    protected int getDelayTime(int waitTime) {
        return waitTime > MAX_WAIT_TIME ? MAX_WAIT_TIME : waitTime;
    }
}
