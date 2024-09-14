package com.jw.media.jvideoplayer.player.base;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import tv.danmaku.ijk.media.player.misc.IMediaDataSource;

/**
 * Created by Joyce.wang on 2024/3/11 14:33
 *
 * @Description A data source that reads from a file.
 */
public class FileMediaDataSource implements IMediaDataSource {
    private RandomAccessFile file;
    private long fileSize;

    /**
     * Creates a new FileMediaDataSource.
     *
     * @param file The file to read from.
     * @throws IOException If an error occurs while opening the file.
     */
    public FileMediaDataSource(File file) throws IOException {
        this.file = new RandomAccessFile(file, "r");
        fileSize = this.file.length();
    }

    @Override
    public int readAt(long position, byte[] buffer, int offset, int size) throws IOException {
        if (file.getFilePointer() != position)
            file.seek(position);

        if (size == 0) {
            return 0;
        }

        return file.read(buffer, 0, size);
    }

    @Override
    public long getSize() throws IOException {
        return fileSize;
    }

    @Override
    public void close() throws IOException {
        if (file != null) {
            fileSize = 0;
            file.close();
            file = null;
        }
    }
}
