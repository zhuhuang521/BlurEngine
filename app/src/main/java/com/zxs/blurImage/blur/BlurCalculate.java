package com.zxs.blurImage.blur;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewTreeObserver;

import com.zxs.blurImage.R;

import java.io.InputStream;

/**
 * Created by zxs on 15/5/19.
 * 高斯模糊计算
 */
public class BlurCalculate {
    private View mView;
    private Context context;
    private Bitmap bitmap;
    private Canvas mCanvas;
    private Rect mRect;
    private Matrix mMatrix;
    private Matrix mDrawMatrix;
    private int realheight, realwidth;
    private int radius = 5;
    private float BITMAP_RATIO = 0.025f;
    private Point point;
    private Rect src,dst;
    public boolean useJni = true;
    public BlurCalculate(Context context, View view) {
        this.context = context;
        this.mView = view;
        mCanvas = new Canvas();
        mRect = new Rect();
        mMatrix = new Matrix();
        mDrawMatrix = new Matrix();
        src = new Rect();
        dst = new Rect();
        point = new Point();

    }

    public void setScale(float scale) {
        BITMAP_RATIO = scale;
    }

    public BlurCalculate(Context context) {
        this.context = context;
        getCpu();
    }

    public Bitmap blurBitmap(Bitmap bitmap) {
        if(bitmap==null){
            return null;
        }
        Bitmap out = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth(), bitmap.getHeight(), false);
        blurByJni(radius, bitmap, out);
        return out;
    }


    public Bitmap blurBitmap(Bitmap bitmap, Bitmap out) {
        if(bitmap==null){
            return BitmapFactory.decodeResource(context.getResources(), R.drawable.blur_bg_white);
        }
        if (out == null) {
            out = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth(), bitmap.getHeight(), false);
        }
        blurByJni(radius, bitmap, out);
        return out;
    }



    public void onDetachedFromWindow() {
        mView.getViewTreeObserver().removeOnPreDrawListener(onPreDrawListener);
        if (bitmap != null)
            bitmap.recycle();
        bitmap = null;
    }

    public void onAttachedToWindow() {
        mView.getViewTreeObserver().addOnPreDrawListener(onPreDrawListener);
    }

    public void setRadius(int arg0) {
        this.radius = arg0;
    }

    public boolean isCanvasChanged(Canvas canvas) {
        return canvas == mCanvas;
    }

    public void BlurCanvas() {
        blurBitmap(bitmap,bitmap);
    }

    public void DrawCanvas(Canvas canvas) {
        if (bitmap != null) {
            canvas.drawBitmap(bitmap, src, dst, null);
            canvas.drawColor(0x77ffffff);
        }else{
            canvas.drawColor(0x77ffffff);
        }
    }

    private void getScreenBitmap() {
        mView.getRootView().destroyDrawingCache();
        mView.getGlobalVisibleRect(mRect, point);
        realheight = mView.getHeight();
        realwidth = mView.getWidth();
        dst.set(0, 0, realwidth, realheight);
        int w = Math.round(realwidth * BITMAP_RATIO);
        int h = Math.round(realheight * BITMAP_RATIO);
        w = w & ~0x03;
        h = h & ~0x03;
        if (w <= 0 || h <= 0)
            return;
        if (bitmap == null || bitmap.getWidth() != w || bitmap.getHeight() != h) {
            bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            mMatrix.setScale(BITMAP_RATIO, BITMAP_RATIO);
            mMatrix.invert(mDrawMatrix);
            src.set(0, 0, w, h);
        }
        float dx = -(Math.min(0, mView.getLeft()) + mRect.left);
        float dy = (-point.y);
        mCanvas.restoreToCount(1);
        mCanvas.setBitmap(bitmap);
        mCanvas.setMatrix(mMatrix);
        mCanvas.translate(dx, dy);
        mCanvas.save();
        mView.getRootView().draw(mCanvas);

    }

    private final ViewTreeObserver.OnPreDrawListener onPreDrawListener = new ViewTreeObserver.OnPreDrawListener() {
        @Override
        public boolean onPreDraw() {
            if (mView.getVisibility() == View.VISIBLE) {
                getScreenBitmap();
            }
            return true;
        }
    };

    private Bitmap blurByJni(int radius, Bitmap in, Bitmap out) {
        Blur(in, out, radius);
        return out;
    }

    static {
        System.loadLibrary("blurjni");
    }

    private static native void Blur(Bitmap in, Bitmap out, int r);

    //获取cpu信息
    private String getCpu() {
        ProcessBuilder cmd;
        String result = "";
        try {
            String[] args = {"/system/bin/cat", "/proc/cpuinfo"};
            cmd = new ProcessBuilder(args);
            Process process = cmd.start();
            InputStream in = process.getInputStream();
            byte[] re = new byte[1024];
            while (in.read(re) != -1) {
                result = result + new String(re);
                return result.toString();
            }
            in.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return result.toString();
    }
}
