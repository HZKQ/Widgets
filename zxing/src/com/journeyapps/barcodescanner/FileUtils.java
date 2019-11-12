package com.journeyapps.barcodescanner;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import androidx.core.content.FileProvider;

/**
 * @author Created by 汪高皖 on 2018/10/10 0010 17:39
 */
@SuppressWarnings("all")
public class FileUtils {
    /**
     * 获取文件或目录大小（目录下所有文件）
     *
     * @param path 文件或目录路径
     * @return 大小，单位字节
     */
    public static long getFileOrDirSize(String path) {
        if (TextUtils.isEmpty(path)) {
            return 0;
        }
        
        return getFileOrDirSize(new File(path));
    }
    
    /**
     * 获取文件或目录大小（目录下所有文件）
     *
     * @param file 要获取大小的文件或目录
     * @return 大小，单位字节
     */
    public static long getFileOrDirSize(File file) {
        if (file == null || !file.exists()) {
            return 0;
        }
        
        if (!file.isDirectory()) {
            return file.length();
        }
        
        long length = 0;
        File[] list = file.listFiles();
        if (list != null) {
            // 文件夹被删除时, 子文件正在被写入, 文件属性异常返回null.
            for (File item : list) {
                length += getFileOrDirSize(item);
            }
        }
        
        return length;
    }
    
    /**
     * 删除文件或文件夹
     *
     * @param path 文件或文件夹地址
     * @return 是否删除成功
     */
    public static boolean delFileOrFolder(String path) {
        return !TextUtils.isEmpty(path) && delFileOrFolder(false, new File(path));
    }
    
    /**
     * 删除文件或文件夹
     *
     * @param file 文件或文件夹
     * @return 是否删除成功
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static boolean delFileOrFolder(File file) {
        return delFileOrFolder(false, file);
    }
    
    /**
     * 只删除文件，不删除文件夹
     *
     * @param path 文件或文件夹地址
     * @return 是否删除成功
     */
    public static boolean justDeleteFile(String path) {
        return !TextUtils.isEmpty(path) && delFileOrFolder(true, new File(path));
    }
    
    /**
     * 只删除文件，不删除文件夹
     *
     * @param file 文件或文件夹
     * @return 是否删除成功
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static boolean justDeleteFile(File file) {
        return delFileOrFolder(true, file);
    }
    
    /**
     * 删除文件或文件夹
     *
     * @param isJustDeleteFile 是否只需要删除文件不删除文件夹
     * @param file             文件或文件夹
     * @return 是否删除成功
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static boolean delFileOrFolder(boolean isJustDeleteFile, File file) {
        if (file == null || !file.exists()) {
            return true;
        } else if (file.isFile()) {
            file.delete();
        } else if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File sonFile : files) {
                    delFileOrFolder(isJustDeleteFile, sonFile);
                }
            }
            
            if (!isJustDeleteFile) {
                file.delete();
            }
        }
        return true;
    }
    
    /**
     * Uri转path
     *
     * @param context 上下文
     * @param uri     Uri
     * @return file path
     */
    public static String getRealFilePath(Context context, final Uri uri) {
        if (null == uri)
            return null;
        final String scheme = uri.getScheme();
        String data = null;
        if (scheme == null)
            data = uri.getPath();
        else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            Cursor cursor = context.getContentResolver().query(uri, new String[]{MediaStore.Images.ImageColumns.DATA}, null, null, null);
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                    if (index > -1) {
                        data = cursor.getString(index);
                    }
                    
                }
                cursor.close();
            }
            if (data == null) {
                data = getImageAbsolutePath(context, uri);
            }
            
        }
        return data;
    }
    
    /**
     * file path转Uri
     *
     * @param context   上下文
     * @param filePath  文件路径
     * @param authority applicationId + .provider
     * @return Uri
     */
    public static Uri filePathToUri(Context context, final String filePath, final String authority) {
        if (Build.VERSION.SDK_INT > 23) {
            return FileProvider.getUriForFile(context, authority,
                new File(filePath));
        } else {
            return Uri.fromFile(new File(filePath));
        }
    }
    
    /**
     * 根据Uri获取图片绝对路径，解决Android4.4以上版本Uri转换
     *
     * @param context  上下文
     * @param imageUri 图片Uri
     * @return 文件路径
     */
    @TargetApi(19)
    public static String getImageAbsolutePath(Context context, Uri imageUri) {
        if (context == null || imageUri == null)
            return null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && DocumentsContract.isDocumentUri(context, imageUri)) {
            if (isExternalStorageDocument(imageUri)) {
                String docId = DocumentsContract.getDocumentId(imageUri);
                String[] split = docId.split(":");
                String type = split[0];
                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            } else if (isDownloadsDocument(imageUri)) {
                String id = DocumentsContract.getDocumentId(imageUri);
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                return getDataColumn(context, contentUri, null, null);
            } else if (isMediaDocument(imageUri)) {
                String docId = DocumentsContract.getDocumentId(imageUri);
                String[] split = docId.split(":");
                String type = split[0];
                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                String selection = MediaStore.Images.Media._ID + "=?";
                String[] selectionArgs = new String[]{split[1]};
                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        } // MediaStore (and general)
        else if ("content".equalsIgnoreCase(imageUri.getScheme())) {
            // Return the remote address
            if (isGooglePhotosUri(imageUri))
                return imageUri.getLastPathSegment();
            return getDataColumn(context, imageUri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(imageUri.getScheme())) {
            return imageUri.getPath();
        }
        return null;
    }
    
    private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        String column = MediaStore.Images.Media.DATA;
        String[] projection = {column};
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }
    
    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }
    
    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }
    
    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
    
    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    private static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }
    
    /**
     * 从assets目录中复制整个文件夹内容,考贝到 /data/data/包名/files/目录中
     *
     * @param ctx      ctx 使用CopyFiles类的Context
     * @param filePath String  文件路径,如：/assets/aa
     */
    public static void copyAssetsDir2Phone(Context ctx, String filePath) {
        String[] fileList = null;
        try {
            fileList = ctx.getAssets().list(filePath);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        
        if (fileList.length > 0) {
            //如果是目录
            File file = new File(ctx.getFilesDir().getAbsolutePath() + File.separator + filePath);
            //如果文件夹不存在，则递归创建
            if (!file.mkdirs()) {
                return;
            }
            
            for (String fileName : fileList) {
                filePath = filePath + File.separator + fileName;
                copyAssetsDir2Phone(ctx, filePath);
                filePath = filePath.substring(0, filePath.lastIndexOf(File.separator));
                Log.e("oldPath", filePath);
            }
        } else {
            //如果是文件
            copyAssetsFile2Phone(ctx, filePath);
        }
    }
    
    /**
     * 将文件从assets目录，考贝到 /data/data/包名/files/ 目录中。assets 目录中的文件，会不经压缩打包至APK包中，使用时还应从apk包中导出来
     *
     * @param fileName 文件名,如aaa.txt
     */
    public static void copyAssetsFile2Phone(Context context, String fileName) {
        //如果是文件
        File file = new File(context.getFilesDir().getAbsolutePath() + File.separator + fileName);
        Log.i("copyAssets2Phone", "file:" + file);
        if (!file.exists() || file.length() == 0) {
            InputStream inputStream = null;
            FileOutputStream fos = null;
            try {
                inputStream = context.getAssets().open(fileName);
                fos = new FileOutputStream(file);
                int len = -1;
                byte[] buffer = new byte[1024];
                while ((len = inputStream.read(buffer)) != -1) {
                    fos.write(buffer, 0, len);
                }
                fos.flush();
            } catch (IOException e) {
                e.printStackTrace();
                return;
            } finally {
                closeQuietly(inputStream);
                closeQuietly(fos);
            }
        } else {
        }
    }
    
    
    public static void closeQuietly(Closeable closeable) {
        if (closeable == null)
            return;
        try {
            closeable.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
