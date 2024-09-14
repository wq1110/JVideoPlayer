package com.jw.media.jvideoplayer.cache.utils;
/**
 * Created by Joyce.wang on 2024/9/11 8:49
 *
 * @Description
 */
public class TimeUtils {
    private static final long DAY = 60 * 60 * 24;
    private static final long HOUR = 60 * 60;
    private static final long MINUTE = 60;

    public static String getVideoTimeString(long duration) {
        duration += 500; // 进位
        duration /= 1000;
        String DateTimes;
        long hours = (duration % (DAY)) / (HOUR);
        long minutes = (duration % (HOUR)) / MINUTE;
        long seconds = duration % MINUTE;
        DateTimes = String.format("%02d:", hours) + String.format("%02d:", minutes) + String.format("%02d", seconds);
        return DateTimes;
    }
}
