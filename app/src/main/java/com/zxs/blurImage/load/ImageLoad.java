package com.zxs.blurImage.load;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.internal.Util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by zxs on 15/9/7.
 * 新的图片加载控件，引入了fresco加载
 */
public class ImageLoad {
    private NetworkInfo networkInfo;
    private static ImageLoad imageLoad;
    private Context mContext;
    public static ImageLoad getInstance(Context context){
        if(imageLoad == null){
            imageLoad = new ImageLoad(context);
        }
        return imageLoad;
    }

    public ImageLoad(Context context){
        this.mContext = context;
    }

    /**
     * 加载图片
     * @param url       图片的连接
     * */
    public ImageLoadControl loadImage(String url){
        return new ImageLoadControl(url,mContext);
    }

    /**
     * 如果图片不能显示，设置默认的图片
     * */
    private void setDefaultImage(){

    }
    /**
     * 是否是wifi
     * */
    public boolean isWifiState(){
        if(networkInfo!=null&&networkInfo.isConnected()){
            if("WIFI".equals(networkInfo.getTypeName())){
                return true;
            }
        }
        return false;
    }

    /**
     * 网络状态变化的时候重新设置网络状态
     * */
    public void setNetworkInfo(NetworkInfo networkInfo){
        this.networkInfo = networkInfo;
    }

    /**
     * 下载图片
     * @param url   图片连接
     * @param path  下载存储的地址
     * */
    public boolean downloadImage(String url , String path,String name){
        //下载图片
        //检查文佳是否存在
        File checkFile = new File(path);
        if(!checkFile.exists()){
            checkFile.mkdirs();
        }
        Request request = new Request.Builder().url(url).build();
        OkHttpClient httpClient = new OkHttpClient();
        String temName = SystemClock.currentThreadTimeMillis()+"";
        File file = new File(path+"/"+temName);
        File finalFile = new File(path+"/"+name);
        try {
            Response response = httpClient.newCall(request).execute();
            if(response.isSuccessful()){
                InputStream inputStream = null;
                FileOutputStream fileOutputStream = null;
                try {
                    inputStream = response.body().byteStream();
                    fileOutputStream = new FileOutputStream(file);
                    byte[] buffer = new byte[4096];
                    int writeLen = 0;
                    int len = 0;
                    while ((len = inputStream.read(buffer, 0, 4096)) != -1) {
                        fileOutputStream.write(buffer, 0, len);
                        writeLen += len;
                    }
                    fileOutputStream.flush();
                } finally {
                    Util.closeQuietly(inputStream);
                    Util.closeQuietly(fileOutputStream);
                }
                file.renameTo(finalFile);
            }else{
                return false;
            }
        }catch (IOException e){
            return false;
        }
        return true;
    }
    /**
     * @param  url 下载的url
     * 冲
     * */
    public Bitmap getBitmap(String url){
        Bitmap bitmap = null;
        Request request = new Request.Builder().url(url).build();
        OkHttpClient httpClient = new OkHttpClient();
        try {
            Response response = httpClient.newCall(request).execute();
            if(response.isSuccessful()){
                InputStream inputStream = null;
                ByteArrayOutputStream dataStream = null;
                try {
                    inputStream = response.body().byteStream();
                    dataStream = new ByteArrayOutputStream();
                    byte[] buffer = new byte[4096];
                    int writeLen = 0;
                    int len = 0;
                    while ((len = inputStream.read(buffer, 0, 4096)) != -1) {
                        dataStream.write(buffer, 0, len);
                        writeLen += len;
                    }
                    byte[] data = dataStream.toByteArray();
                    bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                } finally {
                    Util.closeQuietly(inputStream);
                    dataStream.close();
                }
            }else{
                return null;
            }
        }catch (IOException e){
            return null;
        }
        return bitmap;
    }


    /**
     * 检查是否有图片缓存
     */
    public static boolean hasImageCache(String path,Context context) {
        if (TextUtils.isEmpty(path)) {
            return false;
        }
        File file = new File(getStorePath(context) + "/" + path);
        if (file.exists()) {
            return true;
        }
        return false;
    }

    /**
     * 获得默认存储的父路径
     **/
    public static String getDefaultPath(Context context) {
        File file = context.getExternalFilesDir(null);
        if(file==null){
            return  Environment.getExternalStorageState()+"/Android/data/com.Quhuhu/files/";
        }else{
            if(!file.exists()){
                file.mkdirs();
            }
        }
        return file.toString()+"/";
    }

    public static String getStorePath(Context context) {
        File file = context.getExternalFilesDir(null);
        if(file==null){
            return  Environment.getExternalStorageState()+"/Android/data/com.Quhuhu/files";
        }else{
            if(!file.exists()){
                file.mkdirs();
            }
        }
        return file.toString();
    }
    /**
     * 清除缓存
     */
    public void clearCache() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                File file = new File(getDefaultPath(mContext));
                delete(file);
            }
        });
        thread.start();
    }


    private  void delete(File file) {
        if (file.isFile()) {
            file.delete();
            return;
        }

        if (file.isDirectory()) {
            File[] childFiles = file.listFiles();
            if (childFiles == null || childFiles.length == 0) {
                file.delete();
                return;
            }

            for (int i = 0; i < childFiles.length; i++) {
                delete(childFiles[i]);
            }
            file.delete();
        }
    }
}
