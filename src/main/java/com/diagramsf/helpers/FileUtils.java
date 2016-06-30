package com.diagramsf.helpers;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.StatFs;
import com.diagramsf.net.ExceptionWrapper;

import java.io.*;
import java.net.*;
import java.util.UUID;

public class FileUtils {
    private final static String TAG = "FileUtils";

    public static final String UTF_8 = "UTF-8";

    /** 存放裁剪后的图片的文件夹名称 */
    //    public final static String FILE_CROP_DISK_DIRNAME = "crop";
    /** 存放拍照后的图片的文件夹名称 */
    //    public final static String FILE_CARMER_DISK_DIRNAME = "camera";
    /** 头像下载存放的文件夹名称 */
    //    public final static String FILE_HEAD_CACHE_DISK_DIRNAME = "cache";

    /** 内部缓存目录最大大小 */
    private static final int SIZE_INTERNAL_CACHE = 1024 * 1024;

    // -----------------手机内部存储---start

    /**
     * 获取app在手机内部缓存的临时文件,使用完了 要删除这个文件
     *
     * @return file
     */
    public File getAppInternalTempFile(Context context, String fileName) {
        File file = null;
        try {
            file = File.createTempFile(fileName, null,
                    getAppInternalCacheDir(context));
        } catch (IOException e) {
            // Error while creating file
        }
        return file;
    }

    /**
     * 取得app手机内部存储目录
     *
     * @return dir
     */
    public static File getAppInternalDir(Context context) {
        return context.getFilesDir();
    }

    /**
     * 获得app手机内部缓存目录，系统存储过低的话会自动删除该目录下的文件,这个目录限定最大为 1M
     *
     * @return dir
     */
    public static File getAppInternalCacheDir(Context context) {
        File cacheDir = context.getCacheDir();
        if (cacheDir.exists()) {
            if (getFileSize(cacheDir) >= SIZE_INTERNAL_CACHE) {
                deleteDir(cacheDir);
            }
        }
        return cacheDir;
    }// -----------------手机内部存储---end

    // -----------------手机外部存储---start

    /**
     * 外部存储是否可以读写
     */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();

        // sd卡是否安装了
        boolean isSDcardMounted = Environment.MEDIA_MOUNTED.equals(state);
        // sd卡是否是不可手动移除的
        boolean isSDcardRemovable = isExternalStorageRemovable();
        // sd卡是否正在与电脑共享
        boolean isSDcardShared = Environment.MEDIA_SHARED.equals(state);

        if (isSDcardMounted || (!isSDcardRemovable && !isSDcardShared)) {
            return true;
        }
        return false;
    }

    /**
     * 判断外部存储是否 是只读
     */
    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (state.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
            return true;
        }
        return false;
    }

    /** 判断存储卡是否可以被手动移除 */
    @TargetApi(9)
    public static boolean isExternalStorageRemovable() {
        if (OSVersion.hasGingerbread()) {
            return Environment.isExternalStorageRemovable();
        }
        return true;
    }

    /**
     * 外部存储卡 可用空间
     *
     * @return byte;
     */
    public static long externalStorageAvailableBytes() {
        File extdir = Environment.getExternalStorageDirectory();
        return getUsableSpace(extdir);
    }// -----------------手机外部存储---end

    // -----------------------------------app目录下的外部存储---start

    /**
     * 取得app目录下的外部存储路径，如果为null，就返回内部存储目录
     *
     * @param context       android上下文
     * @param uniqueName    目录名称
     * @param maybeInternal 如果外部存储路径不可用，true的话返回内部存储目录，false 返回null
     *
     * @return dir 或者null
     */
    public static File getAppExternalCacheDir(Context context, String uniqueName, boolean maybeInternal) {
        final boolean isExternalStorageWritable = isExternalStorageWritable();
        File desFile = null;
        if (isExternalStorageWritable) {
            desFile = getAppExternalCacheDir(context);
        }

        if ((null == desFile) && maybeInternal)
            desFile = context.getCacheDir();

        if (null != desFile) {
            String cachePath = desFile.getPath();
            return new File(cachePath + File.separator + uniqueName);
        }

        return null;
    }

    /**
     * 获得app目录下的的外部存储root路径，app卸载后目录会自动被删除
     *
     * @return dir
     */
    @TargetApi(8)
    public static File getAppExternalCacheDir(Context context) {
        if (OSVersion.hasFroyo()) {
            return context.getExternalCacheDir();
        }

        final String cacheDir = "/Android/data/" + context.getPackageName()
                + "/cache/";
        return new File(Environment.getExternalStorageDirectory().getPath()
                + cacheDir);
    }// -----------------------------------app目录下的外部存储---end

    // -------------------------------------外部路径，不在app目录下的---start

    /**
     * 获得android外部存储路径(SD卡)，如果外部存储不可用，就返回app内部存储路径，不一定在app的目录下,
     *
     * @param dirName 外部存储目录名称
     *
     * @return dir 如果外部存储不可用，内部存储路径
     */
    public static File getExternalCacheDir(Context context, String dirName) {
        File cacheFile;
        if (isExternalStorageWritable()) {
            cacheFile = Environment.getExternalStorageDirectory();
            return new File(cacheFile.getPath() + File.separator + dirName);
        } else {
            cacheFile = context.getCacheDir();
        }
        return cacheFile;
    }// -------------------------------------外部路径，不在app目录下的---end

    // --------------------------------------app外部的公共存储路径，卸载后仍会保留

    /**
     * 获得 需要在外部存储的 公共存储区域存储图片的 目录
     *
     * @param dirName 在外部存储的公共图片存储区域 的文件夹名称
     *
     * @return 如果外部存储不可用就返回 null
     */
    public static File getPictureExternalStorageDir(String dirName) {
        File file;
        if (!isExternalStorageWritable()) {
            return null;
        }
        File path = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);

        file = new File(path.getAbsolutePath() + "/" + dirName + "/");
        if (!file.mkdirs()) {
            if (file.exists()) {
                AppDebugLog.v(TAG, dirName + ":" + "公共图片目录已经存在");
            } else {
                AppDebugLog.v(TAG, dirName + ":" + "公共图片目录创建失败");
            }

        }
        return file;
    }


    //----------------------------其它---------start

    /** 删除目录下的所有文件 */
    public static void deleteDir(File dir) {
        if (dir.exists()) {
            if (dir.isDirectory()) {
                File[] files = dir.listFiles();
                for (int i = 0; i < files.length; i++) {
                    File one = files[i];
                    if (one.isDirectory()) {
                        deleteDir(one);
                    } else {
                        one.delete();
                    }
                }
            } else {
                dir.delete();
            }
        }
    }

    /**
     * 给定路径可用空间大小
     *
     * @return bytes
     */
    @TargetApi(9)
    public static long getUsableSpace(File path) {
        if (OSVersion.hasGingerbread()) {
            return path.getUsableSpace();
        }
        final StatFs stats = new StatFs(path.getPath());
        return (long) stats.getBlockSize() * (long) stats.getAvailableBlocks();
    }

    /**
     * 文件夹 大小
     *
     * @return bytes
     */
    public static long getFileSize(File f) {
        long size = 0;
        File flist[] = f.listFiles();
        for (int i = 0; i < flist.length; i++) {
            if (flist[i].isDirectory()) {
                size = size + getFileSize(flist[i]);
            } else {
                size = size + flist[i].length();
            }
        }
        return size;
    }

    /**
     * 获取文件扩展名
     *
     * @param fileName
     *
     * @return
     */
    public static String getFileFormat(String fileName) {
        if (StringUtils.isEmpty(fileName)) {
            return "";
        }
        int point = fileName.lastIndexOf('.');
        return fileName.substring(point + 1);
    }

    /**
     * 根据文件绝对路径获取文件名
     *
     * @param filePath
     *
     * @return
     */
    public static String getFileName(String filePath) {
        if (StringUtils.isEmpty(filePath))
            return "";
        return filePath.substring(filePath.lastIndexOf(File.separator) + 1);
    }

    /** 创建给定目录 */
    public static boolean createDirs(String dirName) {
        File folder = new File(dirName);
        return (folder.exists() && folder.isDirectory()) || folder
                .mkdirs();
    }

    /**
     * 将图片加入到相册中
     */
    public static void galleryAddPic(Context context, String imagePath) {
        if (null != imagePath) {
            Intent mediaScanIntent = new Intent(
                    "android.intent.action.MEDIA_SCANNER_SCAN_FILE");
            File f = new File(imagePath);
            Uri contentUri = Uri.fromFile(f);
            mediaScanIntent.setData(contentUri);
            context.sendBroadcast(mediaScanIntent);
        }
    }

    /**
     * 通知系统，图片已经从相册中删除
     */
    public static void galleryRemovePic(Context context, String imagePath) {
        Intent mediaScanIntent = new Intent(
                "android.intent.action.ACTION_MEDIA_REMOVED");
        File f = new File(imagePath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        context.sendBroadcast(mediaScanIntent);
    }


    //-------------向服务器上传文件 start

    /**
     * 将inputStream写入byte数组
     *
     * @param inputStream
     *
     * @return
     *
     * @throws IOException
     */
    public static byte[] readInputStream(InputStream inputStream)
            throws IOException {
        if (null == inputStream) {
            return null;
        }
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {

            outStream.write(buffer, 0, len);
        }
        outStream.close();
        inputStream.close();

        return outStream.toByteArray();
    }

    /** 上传文件 */
    /**
     * 以post的形式向服务器上传文件
     *
     * @param httpURL        请求的URL连接地址
     * @param postData       向服务器发送的数据
     * @param uploadFilePath 上传文件的路径
     */
    public static String postUploadFile(String httpURL, String postData,
                                        String uploadFilePath, String imageKeyName) throws
        ExceptionWrapper {
        HttpURLConnection httpConnection = getHttpURLConnection(httpURL);
        if (null == httpConnection) {
            return null;
        }
        InputStream is = http_post_upload(httpConnection, postData,
                uploadFilePath, imageKeyName);
        if (null == is) {
            return null;
        }
        String result;
        try {
            result = new String(readInputStream(is), UTF_8);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            throw ExceptionWrapper.run(e);
        } catch (IOException e) {
            e.printStackTrace();
            throw ExceptionWrapper.io(e);
        }

        return result;
    }

    // 获得接口用户代理信息
    public static String getInterfaceUserAgent() {
        return null;
    }

    // 以post的形式请求服务器，上传文件
    private static InputStream http_post_upload(
            HttpURLConnection httpConnection, String postData, String filePath,
            String imageKeyName) throws ExceptionWrapper {

        if (null == httpConnection) {
            return null;
        }

        if (null == filePath) {
            return null;
        }
        String BOUNDARY = UUID.randomUUID().toString(); // 边界标识 随机生成
        String PREFIX = "--", LINE_END = "\r\n";// PREFIX(前缀), LINE_END(行结束)
        String CONTENT_TYPE = "multipart/form-data"; // 内容类型
        try {
            // setRequestProperty()和addRequestProperty()主要是设置HttpURLConnection请求头里面的属性,
            // 这两个方法的区别是，set 是覆盖原来， add 是添加新的，例如：
            // myUrlConnction.setRequestProperty("Accept", "text/html");
            // myUrlConnction.addRequestProperty("Accept", "text/*");
            // or
            // myUrlConnction.setRequestProperty("Accept", "text/html, text/*");
            httpConnection.setRequestMethod("POST");
            httpConnection.setDoInput(true);
            httpConnection.setDoOutput(true);
            httpConnection.setUseCaches(false); // post方式不允许使用缓存
            httpConnection.setRequestProperty("connection", "keep-alive");
            httpConnection.setRequestProperty("Content-Type", CONTENT_TYPE
                    + ";boundary=" + BOUNDARY);
            httpConnection.addRequestProperty("app", "test");
            DataOutputStream dos = new DataOutputStream(
                    httpConnection.getOutputStream());

            StringBuffer sb = new StringBuffer();
            sb.append(PREFIX);
            sb.append(BOUNDARY);
            sb.append(LINE_END);

            // ---------发送postData对应的数据
            if (!StringUtils.isEmpty(postData)) {
                sb.append("Content-Disposition: form-data; name=\"postData\""
                        + LINE_END);
                sb.append("Content-Type: text/plain;charset=UTF-8" + LINE_END
                        + "Content-Length: " + postData.length() + LINE_END
                        + LINE_END);
                sb.append(postData + LINE_END + PREFIX + BOUNDARY + LINE_END);
            }

            // ---------发送app对应的数据
            String app_data = "kimiss";
            sb.append("Content-Disposition: form-data; name=\"app\"" + LINE_END);
            sb.append("Content-Type: text/plain;charset=UTF-8" + LINE_END
                    + "Content-Length: " + app_data.length() + LINE_END
                    + LINE_END);
            sb.append(app_data + LINE_END + PREFIX + BOUNDARY + LINE_END);

            /**
             * 封装要上传的图片文件
             *
             * 这里重点注意： name里面的值为服务器端需要key 只有这个key 才可以得到对应的文件;
             * filename是文件的名字，包含后缀名的 比如:abc.png
             */

            sb.append("Content-Disposition: form-data; name=\"" + imageKeyName
                    + "\"; filename=\"" + filePath + "\"" + LINE_END);

            sb.append("Content-Type: application/octet-stream; charset="
                    + UTF_8 + LINE_END);
            sb.append(LINE_END);

            // 将字符串写入服务器
            dos.write(sb.toString().getBytes());

            // 将上传的文件写入服务器
            InputStream is = new FileInputStream(filePath);
            byte[] bytes = new byte[1024];
            int len = 0;
            while ((len = is.read(bytes)) != -1) {
                dos.write(bytes, 0, len);
            }
            is.close();
            // 写入结尾符
            dos.write(LINE_END.getBytes());
            byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINE_END)
                    .getBytes();
            dos.write(end_data);
            dos.flush();

            InputStream input = httpConnection.getInputStream();
            return input;

        } catch (ProtocolException e) {// 配置 HttpURLConnection时可能报这个异常
            e.printStackTrace();
            throw ExceptionWrapper.http(e);
        } catch (IOException e) {// connect()和从网络上读数据的时候 可能报这个异常
            e.printStackTrace();
            httpConnection.disconnect();
            throw ExceptionWrapper.io(e);
        }

    }

    // 取得HttpURLConnection
    private static HttpURLConnection getHttpURLConnection(String url)
            throws ExceptionWrapper {
        URL httpURL;
        URLConnection connection;
        HttpURLConnection httpConnection;
        try {
            httpURL = new URL(url);
            connection = httpURL.openConnection();
            if (!(connection instanceof HttpURLConnection)) {
                // 发生错误，打开的连接不是http连接
                return null;
            }
            httpConnection = (HttpURLConnection) connection;
            httpConnection.setConnectTimeout(1000 * 15);// 设置连接超时时间
            httpConnection.setReadTimeout(20000);// 设置读取超时时间
            httpConnection.setRequestProperty("Charset", UTF_8);
            httpConnection.setRequestProperty("User-Agent",
                    getInterfaceUserAgent());
            httpConnection.setUseCaches(false);
        } catch (MalformedURLException e) { // String 转换成URL时可能会报这个错误
            e.printStackTrace();
            throw ExceptionWrapper.run(e);
        } catch (IOException e) { // 当 openConnection()时可能会报这个异常
            e.printStackTrace();
            throw ExceptionWrapper.io(e);
        }

        return httpConnection;
    }//-------------向服务器上传文件 end


    /**
     * 复制整个文件夹内容
     *
     * @param oldPath String 原文件路径 如：c:/fqf
     * @param newPath String 复制后路径 如：f:/fqf/ff
     *
     * @return boolean
     */
    public static void copyFolder(String oldPath, String newPath) {
        try {
            (new File(newPath)).mkdirs(); //如果文件夹不存在 则建立新文件夹
            File a = new File(oldPath);
            String[] file = a.list();
            File temp;
            for (int i = 0; i < file.length; i++) {
                if (oldPath.endsWith(File.separator)) {
                    temp = new File(oldPath + file[i]);
                } else {
                    temp = new File(oldPath + File.separator + file[i]);
                }

                if (temp.isFile()) {
                    FileInputStream input = new FileInputStream(temp);
                    FileOutputStream output = new FileOutputStream(newPath + "/" +
                            (temp.getName()));
                    byte[] b = new byte[1024 * 5];
                    int len;
                    while ((len = input.read(b)) != -1) {
                        output.write(b, 0, len);
                    }
                    output.flush();
                    output.close();
                    input.close();
                }
                if (temp.isDirectory()) {//如果是子文件夹
                    copyFolder(oldPath + "/" + file[i], newPath + "/" + file[i]);
                }
            }
        } catch (Exception e) {
            System.out.println("复制整个文件夹内容操作出错");
            e.printStackTrace();
        }
    }

    /**
     * 复制单个文件
     *
     * @param oldPath String 原文件路径 如：c:/fqf.txt
     * @param newPath String 复制后路径 如：f:/fqf.txt
     *
     * @return boolean
     */
    public static boolean copyFile(String oldPath, String newPath) {
        boolean isok = true;
        try {
            int byteread;
            File oldfile = new File(oldPath);
            if (oldfile.exists()) { //文件存在时
                InputStream inStream = new FileInputStream(oldPath); //读入原文件
                FileOutputStream fs = new FileOutputStream(newPath);
                byte[] buffer = new byte[1024];
                while ((byteread = inStream.read(buffer)) != -1) {
                    fs.write(buffer, 0, byteread);
                }
                fs.flush();
                fs.close();
                inStream.close();
            } else {
                System.out.println("复制单个文件元文件不存在");
                isok = false;
            }
        } catch (Exception e) {
            System.out.println("复制单个文件操作出错");
            e.printStackTrace();
            isok = false;
        }
        return isok;
    }

}
