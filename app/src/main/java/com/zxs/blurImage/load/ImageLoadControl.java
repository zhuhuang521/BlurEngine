package com.zxs.blurImage.load;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.text.TextUtils;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.backends.pipeline.PipelineDraweeControllerBuilder;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.request.BasePostprocessor;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.facebook.imagepipeline.request.Postprocessor;
import com.zxs.blurImage.blur.BlurCalculate;
import com.zxs.blurImage.photodraweeview.PhotoDraweeView;

/**
 * Created by zxs on 15/9/10.
 * 新的图片加载控制器
 */
public class ImageLoadControl {
    private String url;
    //是否要忽略网络限制
    private boolean ignoreNet = false;
    //默认加载的占位图
    private int loadImage = 0;
    //加载失败的占位图
    private int errorImage = 0;
    //下载的图片是否要模糊处理
    private boolean isBlur = false;
    private int radius = 5;

    private Context mContext;
    //支持不支持缩放
    private boolean couldScale = false;

    private float scale = 10f;


    public ImageLoadControl(String url, Context context){
        this.url = url;
        mContext = context;
    }

    /**
     * 是否忽略当前的网络状态下载图片
     * @param  ignore
     * */
    public ImageLoadControl ignoreNetState(boolean ignore){
        this.ignoreNet = ignore;
        return this;
    }

    /**
     * 显示加载的占位图
     * */
    public ImageLoadControl setDefaultImage(int loadId,int errorId){
        this.loadImage = loadId;
        this.errorImage = errorId;
        return this;
    }

    /**
     *设置是否要设置模糊，以及模糊半径
     * */
    public ImageLoadControl setBlur(boolean blur,int radius){
        this.isBlur = blur;
        this.radius = radius;
        return this;
    }
    public ImageLoadControl setBlur(boolean blur){
        this.isBlur = blur;
        return this;
    }

    public ImageLoadControl setBlur(boolean blur,int radius,float scale){
        this.isBlur = blur;
        this.radius = radius;
        this.scale = scale;
        return this;
    }

    /**
     * 设置图片控件是否可以进行相应的缩放
     * */
    public ImageLoadControl setCouldScale(boolean couldScale){
        this.couldScale = couldScale;
        return this;
    }
    public void load(final SimpleDraweeView imageView){
        if(TextUtils.isEmpty(url)){
            //如果连接为空直接加载默认的图片
            return;
        }
        Postprocessor postprocessor;
        if(isBlur){
            postprocessor = new BlurPostprocessor();
        }else{
            postprocessor = new BasePostprocessor() {
                @Override
                public String getName() {
                    return super.getName();
                }
            };
        }
        ImageRequest request;
        //如果已经开启了网络控制，切不是忽略的网路图片加载本地图片
        if(!ignoreNet&&!true&&!true){

            request = ImageRequestBuilder.newBuilderWithSource(Uri.parse(url))
                    .setPostprocessor(postprocessor).setLowestPermittedRequestLevel(ImageRequest.RequestLevel.DISK_CACHE).build();
            return;
        }else{
            request = ImageRequestBuilder.newBuilderWithSource(Uri.parse(url))
                    .setPostprocessor(postprocessor).setLowestPermittedRequestLevel(ImageRequest.RequestLevel.FULL_FETCH).build();
        }
        PipelineDraweeControllerBuilder controller = Fresco.newDraweeControllerBuilder();
        controller.setImageRequest(request);
        controller.setOldController(imageView.getController());
        if(couldScale&& imageView instanceof PhotoDraweeView) {
            controller.setControllerListener(new BaseControllerListener<ImageInfo>() {
                @Override
                public void onFinalImageSet(String id, ImageInfo imageInfo, Animatable animatable) {
                    super.onFinalImageSet(id, imageInfo, animatable);
                    if (imageInfo == null || imageView == null) {
                        return;
                    }
                    ((PhotoDraweeView) imageView).update(imageInfo.getWidth(), imageInfo.getHeight());
                }
            });
        }
        imageView.setController(controller.build());
    }

    private static Bitmap small(Bitmap bitmap,float scale) {
        Matrix matrix = new Matrix();
        matrix.postScale(scale,scale);
        Bitmap resizeBmp = Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);
        return resizeBmp;
    }

    /**
     * 检查当前图片是否有本地缓存图片
     * */
    private boolean hasCache(String url){
        return false;
    }

    //模糊处理下载的图片
    private class BlurPostprocessor extends  BasePostprocessor{
        @Override
        public String getName() {
            return super.getName();
        }
        @Override
        public void process(Bitmap bitmap) {
            BlurCalculate blurCalculate = new BlurCalculate(mContext);
            blurCalculate.setRadius(radius);
            Bitmap blurBit = small(bitmap,1/scale);
            Rect src,dst;
            blurBit = blurCalculate.blurBitmap(blurBit);
            Canvas canvas = new Canvas();
            canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
            canvas.setBitmap(bitmap);
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            blurBit = small(blurBit,scale);
            src = new Rect(0,0,blurBit.getWidth(),blurBit.getHeight());
            dst = new Rect(0,0,bitmap.getWidth(),bitmap.getHeight());
            canvas.drawBitmap(blurBit, src, dst, paint);
            if(blurBit!=null){
                blurBit.recycle();
            }
            super.process(bitmap);
        }
    }
}
