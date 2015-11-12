package com.zxs.blurImage.blur;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * Created by zxs on 15/5/19.
 * 模糊布局的实现，使用有两种模式
 * MODE 1:
 *      模糊该布局布局层级下的所有布局内容
 * MODE 2:
 *      模糊该布局的背景 background内容
 */
public class BlurFrameLayout extends FrameLayout {
    //默认模式，模糊层级下的内容
    private int mode =0;
    private BlurCalculate calculate;
    public BlurFrameLayout(Context context) {
        this(context,null);
    }

    public BlurFrameLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BlurFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initBlur(context);
    }
    //初始化blur计算
    private void initBlur(Context context){
        calculate = new BlurCalculate(context,this);
        calculate.setRadius(4);
        calculate.setScale(0.05f);
    }

    public void setMode(int mode){
        this.mode = mode;
    }

    public void setRadius(int arg0){
        if(calculate!=null){
            calculate.setRadius(arg0);
            invalidate();
        }
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {

        if(mode==0){
            if(calculate.isCanvasChanged(canvas)){
                calculate.BlurCanvas();
            }else{
                calculate.DrawCanvas(canvas);
                super.dispatchDraw(canvas);
            }

        }else{
            super.dispatchDraw(canvas);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        calculate.onDetachedFromWindow();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        calculate.onAttachedToWindow();
    }
}
