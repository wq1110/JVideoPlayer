package com.jw.media.jvideoplayer.lib.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.jw.media.jvideoplayer.lib.log.Logger;
import com.jw.media.jvideoplayer.lib.log.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import static org.apache.commons.io.FileUtils.cleanDirectory;

import org.apache.commons.io.FilenameUtils;

/**
 * Created by Joyce.wang on 2024/9/12 13:29
 *
 * @Description TODO
 */
public class FileUtils {
    private static Logger logger = LoggerFactory.getLogger(FileUtils.class.getSimpleName());

    private static HashMap<String, String> sOverrideMap;

    static {
        sOverrideMap = new HashMap<>();
        sOverrideMap.put("tr", "ISO-8859-9");
        sOverrideMap.put("sr", "Windows-1250");
    }

    /**
     * Get contents of a file as String
     *
     * @param filePath File path as String
     * @return Contents of the file
     */
    public static String getContentsAsString(String filePath) throws IOException {
        File fl = new File(filePath);
        FileInputStream fin = new FileInputStream(fl);
        String ret = convertStreamToString(fin);
        //Make sure you close all streams.
        fin.close();
        return ret;
    }

    /**
     * Convert an {@link InputStream} to a String
     *
     * @param inputStream InputStream
     * @return String contents of the InputStream
     */
    private static String convertStreamToString(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        reader.close();
        return sb.toString();
    }

    /**
     * Delete every item below the File location
     *
     * @param file Location
     */
    public static boolean recursiveDelete(File file) {
        if (file.isDirectory()) {
            String[] children = file.list();
            if (children == null) return false;
            for (String child : children) {
                recursiveDelete(new File(file, child));
            }
        }

        return file.delete();
    }


    /**
     * Save {@link String} to {@link File} witht the specified encoding
     *
     * @param string {@link String}
     * @param path   Path of the file
     * @param string Encoding
     */
    public static void saveStringToFile(String string, File path, String encoding) throws IOException {
        if (path.exists()) {
            path.delete();
        }

        if ((path.getParentFile().mkdirs() || path.getParentFile().exists()) && (path.exists() || path.createNewFile())) {
            Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path), encoding));
            writer.write(string);
            writer.close();
        }
    }

    public static void stringToFile(File file, String string) throws IOException {
        stringToFile(file, string, false);
    }

    public static void stringToFile(File file, String string, boolean append) throws IOException {
        FileWriter out = new FileWriter(file, append);
        try {
            out.write(string);
        } finally {
            out.close();
        }
    }

    /**
     * Get the extension of the file
     *
     * @param fileName Name (and location) of the file
     * @return Extension
     */
    public static String getFileExtension(String fileName) {
        String extension = "";

        int i = fileName.lastIndexOf('.');
        int p = Math.max(fileName.lastIndexOf('/'), fileName.lastIndexOf('\\'));

        if (i > p) {
            extension = fileName.substring(i + 1);
        }

        return extension;
    }


    /**
     * 选择的文件是名称
     *
     * @param uri
     * @return
     */
    public static String getFileNameByUri(Context context, Uri uri) {
        if (context == null || uri == null) {
            return "";
        }
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            String name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME));
            cursor.close();
            return name;
        }
        return "";
    }


    /**
     * Copy file (only use for files smaller than 2GB)
     *
     * @param src Source
     * @param dst Destionation
     */
    public static void copy(File src, File dst) throws IOException {
        FileInputStream inStream = new FileInputStream(src);
        FileOutputStream outStream = new FileOutputStream(dst);
        FileChannel inChannel = inStream.getChannel();
        FileChannel outChannel = outStream.getChannel();
        inChannel.transferTo(0, inChannel.size(), outChannel);
        inStream.close();
        outStream.close();
    }


    /**
     * 设计到外部sd卡存储的，要使用这个方法，通过文件选择器反馈的uri来读取文件
     *
     * @param context
     * @param uri
     * @param dest
     */
    public static void copy(Context context, Uri uri, File dest) {
        try {
            InputStream inStream = context.getContentResolver().openInputStream(uri);
            if (inStream != null) {
                createParent(dest);
                FileOutputStream fos = new FileOutputStream(dest);

                if (inStream instanceof FileInputStream) {
                    FileInputStream fin = (FileInputStream) inStream;
                    FileChannel inChannel = fin.getChannel();
                    FileChannel outChannel = fos.getChannel();
                    inChannel.transferTo(0, inChannel.size(), outChannel);
                    inChannel.close();
                    outChannel.close();
                } else {
                    BufferedInputStream bis = new BufferedInputStream(inStream);
                    BufferedOutputStream bos = new BufferedOutputStream(fos);
                    byte[] byteArray = new byte[1024];
                    int bytes = 0;
                    while ((bytes = bis.read(byteArray)) != -1) {
                        bos.write(byteArray, 0, bytes);
                    }
                    bos.close();
                    fos.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void createParent(File file) {
        try {
            if (file == null) {
                return;
            }
            if (file.getParentFile() != null) {
                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }
            } else {
                if (!TextUtils.isEmpty(file.getParent())) {
                    new File(file.getParent()).mkdirs();
                }
            }
        } catch (Exception e) {
            logger.e("create parent error : " + e);
        }
    }

    public static void findFilesWithExtension(String baseDir, String extention, List<File> result) {
        File dir = new File(baseDir);
        if (!dir.exists() || !dir.isDirectory()) return;

        File[] files = dir.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (!file.isDirectory()) {
                if (FilenameUtils.getExtension(file.getAbsolutePath()).equalsIgnoreCase(extention)) {
                    result.add(file);
                }
            } else {
                findFilesWithExtension(file.getAbsolutePath(), extention, result);
            }
        }
    }

    public static boolean makeDir(String path) {
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdir();
            return true;
        } else if (dir.exists() && !dir.isDirectory()) {
            return false;
        } else {
            return true;
        }
    }

    public static void unzip(File zipFile, File targetDirectory) throws IOException {
        ZipInputStream zis = new ZipInputStream(
                new BufferedInputStream(new FileInputStream(zipFile)));
        try {
            ZipEntry ze;
            int count;
            byte[] buffer = new byte[8192];
            while ((ze = zis.getNextEntry()) != null) {
                File file = new File(targetDirectory, ze.getName());
                File dir = ze.isDirectory() ? file : file.getParentFile();
                if (!dir.isDirectory() && !dir.mkdirs())
                    throw new FileNotFoundException("Failed to ensure directory: " + dir.getAbsolutePath());
                if (ze.isDirectory())
                    continue;
                FileOutputStream fout = new FileOutputStream(file);
                try {
                    while ((count = zis.read(buffer)) != -1)
                        fout.write(buffer, 0, count);
                } finally {
                    fout.close();
                }
            }
        } finally {
            zis.close();
        }
    }

    public static void save2File(String filename, byte[] data, int offset, int len) {
        File dumpFile = new File(filename);
        if (!dumpFile.exists()) {
            try {
                dumpFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        BufferedOutputStream bos = null;
        FileOutputStream fos = null;
        FileWriter fileWriter = null;
        try {
            fos = new FileOutputStream(dumpFile, true);
            bos = new BufferedOutputStream(fos);
            bos.write(data, offset, len);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    public static boolean deleteQuietly(final String path) {
        File file = new File(path);
        return deleteQuietly(file);
    }

    public static boolean deleteQuietly(final File file) {
        if (file == null) {
            return false;
        }
        try {
            if (file.isDirectory()) {
                cleanDirectory(file);
            }
        } catch (final Exception ignored) {
        }

        try {
            return file.delete();
        } catch (final Exception ignored) {
            return false;
        }
    }


    //delete file or Directory
    public static boolean deleteFileOrDirectory(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            return false;
        } else {
            if (file.isFile()) {
                // 为文件时调用删除文件方法
                return deleteFile(filePath);
            } else {
                // 为目录时调用删除目录方法
                return deleteDirectory(filePath);
            }
        }
    }

    //delete file
    public static boolean deleteFile(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return true;
        }
        File file = new File(filePath);
        if (file.isFile() && file.exists()) {
            return file.delete();
        }
        return false;
    }

    //delete Directory
    public static boolean deleteDirectory(String filePath) {
        boolean flag = false;
        if (!filePath.endsWith(File.separator)) {
            filePath = filePath + File.separator;
        }
        File dirFile = new File(filePath);
        if (!dirFile.exists() || !dirFile.isDirectory()) {
            return false;
        }
        flag = true;
        File[] files = dirFile.listFiles();
        //遍历删除文件夹下的所有文件(包括子目录)
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    //删除子文件
                    flag = deleteFile(file.getAbsolutePath());
                    if (!flag) break;
                } else {
                    //删除子目录
                    flag = deleteDirectory(file.getAbsolutePath());
                    if (!flag) break;
                }
            }
        }

        if (!flag) return false;
        //删除当前空目录
        return dirFile.delete();
    }

    public static void copyFileFromAssets(Context context, String filename, String destinationPath) throws IOException {
        AssetManager assetManager = context.getAssets();
        InputStream in = null;
        OutputStream out = null;
        try {
            in = assetManager.open(filename);
            File outFile = new File(destinationPath, filename);
            if (!outFile.getParentFile().exists()) {
                outFile.getParentFile().mkdirs(); // 如果目标目录不存在，则创建
            }
            out = new FileOutputStream(outFile);
            copyFile(in, out);
        } finally {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
        }
    }

    private static void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }
}
