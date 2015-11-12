package com.zxs.blurImage.blur;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.zxs.blurImage.R;


/**
 * Created by zxs on 15/6/22.
 */
public class StaticBlurImageView extends FrameLayout {

    private ImageView defaultImage;
    private ImageView blurImageView;
    private Context mContext;
    private Bitmap bitmap;
    private BlurCalculate blurCalculate;
    private float scale = 0.1f;
    private Bitmap blurredBitmap;
    private Animation anim;
    private Animation anim2;
    private BlurTask blurTask;
    //添加设置模糊的效果
    private boolean autoBlur = true;
    public StaticBlurImageView(Context context) {
        this(context, null);
    }

    public StaticBlurImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        defaultImage = new ImageView(context);
        blurImageView = new ImageView(context);
        defaultImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
        blurImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        this.addView(defaultImage);
        this.addView(blurImageView);
        blurCalculate = new BlurCalculate(context);
        blurCalculate.setScale(0.05f);
        if(blurCalculate.useJni){
            scale = 0.1f;
            blurCalculate.setRadius(5);
        }else{
            scale = 0.2f;
            blurCalculate.setRadius(10);
        }


    }

    public void setAutoBlur(boolean flag){
        autoBlur = flag;
        if(!autoBlur){
            blurImageView.setAlpha(0f);
        }
    }
    public void setBlurAlpha(float alpha){
        alpha = Math.max(alpha, 0f);
        alpha = Math.min(1f, alpha);
        blurImageView.setAlpha(alpha);
    }
    public void setImageDrawable(Drawable drawable){
        bitmap = ((BitmapDrawable)drawable).getBitmap();
        defaultImage.setImageBitmap(bitmap);
        if(bitmap==null){
            return ;
        }
        startBlur();
    }

    public void setImageBitmap(Bitmap bm){
        this.bitmap = bm;
        defaultImage.setImageBitmap(bitmap);
        if(bitmap==null){
            return;
        }
        startBlur();
    }

    @Override
    protected void onDetachedFromWindow() {
        try{
            if(anim!=null){
                anim.cancel();
            }
            if(anim2!=null){
                anim2.cancel();
            }
            blurImageView.setImageBitmap(null);
            defaultImage.setImageBitmap(null);
            blurImageView.setImageDrawable(null);
            defaultImage.setImageDrawable(null);
            if(bitmap!=null&&!bitmap.isRecycled()){
                //bitmap.recycle();
            }
            if(blurredBitmap!=null&&!blurredBitmap.isRecycled()){
                blurredBitmap.recycle();
            }
            bitmap = null;
            blurredBitmap = null;
            blurImageView = null;
            defaultImage = null;
        }catch(Exception e){}
        super.onDetachedFromWindow();

    }

    private void startBlur(){
        blurTask = new BlurTask();
        blurTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    public class BlurTask extends AsyncTask<Void, Void, Bitmap> {

        public BlurTask() {
            this(false);
        }

        public BlurTask(boolean onlyReBlur) {

        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Bitmap doInBackground(Void... voids) {
            if(bitmap==null||bitmap.isRecycled()){
                return null;
            }
            blurredBitmap = Bitmap.createScaledBitmap(bitmap, (int) (bitmap.getWidth() * scale), (int) (bitmap.getHeight() * scale), true);
            try {
                blurredBitmap = blurCalculate.blurBitmap(blurredBitmap,null);
            } catch (Exception e) {
            }
            return  blurredBitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bm) {
            if(bm != null&&blurredBitmap!=null&&!blurredBitmap.isRecycled()&&!bitmap.isRecycled()) {
                blurImageView.setImageBitmap(blurredBitmap);
                if(!autoBlur){
                    return;
                }
                anim = AnimationUtils.loadAnimation(mContext, R.anim.anim_alpha_out);
                anim.setDuration(2000);
                anim.setFillAfter(true);
                anim.setStartOffset(200);
                anim2 = AnimationUtils.loadAnimation(mContext, R.anim.anim_alpha_in);
                anim2.setDuration(2000);
                anim2.setStartOffset(200);
                anim2.setFillAfter(true);
                blurImageView.startAnimation(anim);
                //defaultImage.startAnimation(anim2);
                anim2.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        try{
                            blurImageView.setImageBitmap(null);
                            blurredBitmap.recycle();
                            blurImageView.setVisibility(View.GONE);
                        }catch (Exception e){}
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
            }
        }
    }
}
