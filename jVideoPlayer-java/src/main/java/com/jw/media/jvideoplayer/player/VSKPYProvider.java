package com.jw.media.jvideoplayer.player;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.jw.media.jvideoplayer.player.play.IPlay;
import com.jw.media.jvideoplayer.player.play.PlayImpl;

/**
 * Created by Joyce.wang on 2024/9/13 13:14
 *
 * @Description TODO
 */
public class VSKPYProvider extends ContentProvider {
    @Override
    public boolean onCreate() {
        SdkServiceManager.registerService(IPlay.class, new PlayImpl());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        return null;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return "";
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }
}
