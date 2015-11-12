package com.zxs.blurImage.blur;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.Scroller;

import com.zxs.blurImage.R;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by zxs on 15/5/20.
 * 模糊imageView自身效果
 */
public class BlurImageView extends ImageView {
    private Bitmap mBitmap;
    //是否给设置了图片
    private boolean imageFlag = false;
    private int minRadius,maxRadius;
    private boolean autoBlur = false;
    private int radius = 10;
    private Scroller scroller;
    private int during = 500;
    private float scale;
    public boolean first = true;
    private BlurCalculate blurCalculate;
    private Drawable setDrawable;
    private ExecutorService exec = Executors.newSingleThreadExecutor();
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(autoBlur){
                initView(setDrawable);
                if (mBitmap != null) {
                    startAutoBlur(minRadius,maxRadius);
                }
            }else{
                initView(setDrawable);
                if(mBitmap!=null){
                    BlurImageView.super.setImageDrawable(new BitmapDrawable(blurCalculate.blurBitmap(mBitmap, null)));
                    //super.setImageDrawable(new BitmapDrawable(mBitmap));
                   // super.setImageDrawable(drawable);
                }
            }
        }
    };
    public BlurImageView(Context context) {
        this(context, null);
    }

    public BlurImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BlurImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        blurCalculate = new BlurCalculate(context);
        final TypedArray a = context.obtainStyledAttributes(
                attrs, R.styleable.BlurImageView, defStyleAttr, defStyleAttr);
        Drawable d = a.getDrawable(R.styleable.BlurImageView_blurImage);
        minRadius = a.getInteger(R.styleable.BlurImageView_minRadius, 1);
        maxRadius = a.getInteger(R.styleable.BlurImageView_maxRadius, 15);
        maxRadius = Math.min(maxRadius, 24);
        autoBlur = a.getBoolean(R.styleable.BlurImageView_autoBlur, false);
        during = a.getInteger(R.styleable.BlurImageView_during,4000);
        int finalRadius = a.getInteger(R.styleable.BlurImageView_finalRadius,5);
        if(!autoBlur){
            blurCalculate.setRadius(finalRadius);
        }
        int defaultScale = 3;
        int scaleNum = a.getInteger(R.styleable.BlurImageView_scale,defaultScale);
        scale = 1f/scaleNum;
        a.recycle();
        if(d!=null){
            first = false;
            imageFlag = true;
            initView(d);
        }
        scroller = new Scroller(getContext());
    }

    private void initView(Drawable drawable){
        scroller = new Scroller(getContext());
        if(imageFlag&&drawable!=null&&drawable.getIntrinsicHeight()>0&&drawable.getIntrinsicHeight()>0){
            int w = drawable.getIntrinsicWidth();
            int h = drawable.getIntrinsicHeight();
            if((int)(w*scale)/4*4<=0||(int)(h*scale)/4*4<=0){
                return ;
            }
            Bitmap.Config config =
                    drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                            : Bitmap.Config.ARGB_8888;
            mBitmap = Bitmap.createBitmap(w, h, config);
            Canvas canvas = new Canvas(mBitmap);
            drawable.setBounds(0, 0, w, h);
            drawable.draw(canvas);

            mBitmap = Bitmap.createScaledBitmap(mBitmap, (int) (w * scale) / 4 * 4, (int) (h * scale) / 4 * 4, false);

        }
    }

    private void initBitmap(Bitmap bitmap){
        scroller = new Scroller(getContext());
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        if((int)(w*scale)/4*4<=0||(int)(h*scale)/4*4<=0){
            return ;
        }
        Bitmap.Config config = Bitmap.Config.ARGB_8888;
        mBitmap = Bitmap.createBitmap(w, h, config);
        mBitmap = Bitmap.createScaledBitmap(bitmap, (int) (w * scale) / 4 * 4, (int) (h * scale) / 4 * 4, false);
    }

    public void setRadius(int radius){
        if(this.radius == radius){
            return;
        }
        this.radius = radius;
        if(mBitmap!=null){
            blurCalculate.setRadius(radius);
            super.setImageDrawable(new BitmapDrawable(blurCalculate.blurBitmap(mBitmap, null)));
        }
    }
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if(imageFlag&&mBitmap!=null) {
            if(!autoBlur) {
                super.setImageDrawable(new BitmapDrawable(blurCalculate.blurBitmap(mBitmap, null)));
            }else{
                startAutoBlur(minRadius,maxRadius);
            }
        }
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if(scroller.computeScrollOffset()){
            startTask(scroller.getCurrX());
            this.invalidate();
        }
    }

    //开始自动模糊
    private void startAutoBlur(int min,int max){
        if(scroller!=null){
            scroller.forceFinished(true);
        }
        scroller.forceFinished(true);
        exec.shutdown();
        exec = Executors.newSingleThreadExecutor();
        startTask(1);
        scroller.startScroll(min,0,max-min,0,during);
        invalidate();
    }

    private void startTask(int radius){
        AutoTask autoTask = new AutoTask(radius);
        autoTask.executeOnExecutor(exec);
    }

    private class AutoTask extends AsyncTask<Void,Void,Bitmap> {
        private int radius;
        public AutoTask(int radius){
            this.radius = radius;
        }

        @Override
        protected Bitmap doInBackground(Void... voids) {
            blurCalculate.setRadius(radius);
            return blurCalculate.blurBitmap(mBitmap,null);
        }

        @Override
        protected void onPostExecute(Bitmap o) {
            //super.onPostExecute(o);
            BlurImageView.super.setImageDrawable(new BitmapDrawable(o));
        }
    }
    @Override
    public void setImageResource(int resId) {
        super.setImageResource(resId);
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        if(drawable==null){
            super.setImageDrawable(null);
            return;
        }
        if(first&&autoBlur&&drawable!=null&&drawable.getIntrinsicHeight()>0&&drawable.getIntrinsicHeight()>0){
            setDrawable =  drawable;
            imageFlag = true;
            handler.sendEmptyMessageDelayed(0,100);

        }else if(first&&!autoBlur&&drawable!=null&&drawable.getIntrinsicHeight()>0&&drawable.getIntrinsicHeight()>0){
            setDrawable =  drawable;
            imageFlag = true;
            handler.sendEmptyMessageDelayed(0,100);
        }

    }
}
