package com.elapse.custom_signview;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Picture;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PictureDrawable;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import junit.framework.Test;

import java.util.List;

/**
 * Created by YF_lala on 2018/8/25.
 */

public class CustomView extends View {
    private static final String TAG = "CustomView";
    private Paint mPaint_signup,mPaint_signdown,mTextPaint,mSpecialItemPaint;
    private int itemCount;
    private int sign_upId,sign_downId;
    private String[] itemText;
    private float itemTextSize;
    private Rect rect_src,rect_dest,mSign_down_rect,textBounds;
    private int itemSize;
    private final int DEFAULT_ITEM_SIZE = 20;
    private Bitmap icon_sign_up,icon_sign_down;
    private Picture mPicture_up,mPicture_down;
    private PictureDrawable drawable;
    private SharedPreferences prefs;
    private int signed_days;

    public CustomView(Context context) {
        this(context,null);
    }

    public CustomView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public CustomView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray ta = context.obtainStyledAttributes(attrs,R.styleable.CustomView);
        itemCount = ta.getInt(R.styleable.CustomView_item_count,7);
        sign_upId = ta.getResourceId(R.styleable.CustomView_item_icon_sign_up,R.drawable.sign_up);
        sign_downId = ta.getResourceId(R.styleable.CustomView_item_icon_sign_down,R.drawable.sign_down);
//        itemText = ta.getString(R.styleable.CustomView_item_text);
        itemTextSize = ta.getDimension(R.styleable.CustomView_item_textSize,8);
        ta.recycle();
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        init();
    }

    private void init() {
        mPaint_signup = new Paint();
        mPaint_signup.setColor(Color.GREEN);
        mPaint_signup.setStrokeWidth(2.0f);
        mPaint_signup.setAntiAlias(true);

        mPaint_signdown = new Paint();
        mPaint_signdown.setColor(Color.GRAY);
        mPaint_signdown.setStrokeWidth(2.0f);
        mPaint_signdown.setAntiAlias(true);

        mTextPaint = new Paint();
        mTextPaint.setColor(Color.GRAY);
        mTextPaint.setTextSize(itemTextSize);

        mSpecialItemPaint = new Paint();
        mSpecialItemPaint.setColor(Color.RED);
        mSpecialItemPaint.setTextSize(itemTextSize+4);
        textBounds = new Rect();
        //test data
        itemText = new String[itemCount];
        for (int i=0;i<itemCount;i++){
            itemText[i] = "+"+i;
        }

        signed_days = prefs.getInt("num_of_day",0);

        icon_sign_up = BitmapFactory.decodeResource(getResources(),sign_upId);
        icon_sign_down = BitmapFactory.decodeResource(getResources(),sign_downId);
        rect_src = new Rect(0,0,icon_sign_up.getWidth(),icon_sign_up.getHeight());
        setLayerType(View.LAYER_TYPE_HARDWARE,null);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec)-getPaddingLeft()-getPaddingRight();
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec)-getPaddingBottom()-getPaddingTop();
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (widthMode == MeasureSpec.AT_MOST || heightMode == MeasureSpec.AT_MOST){
            width = DEFAULT_ITEM_SIZE*(itemCount*2-1);
            height =DEFAULT_ITEM_SIZE*2;
        }
        itemSize = width/(itemCount*2-1);
        if (rect_dest == null){
            rect_dest = new Rect(0,height-itemSize,itemSize,height);
        }
        if (mSign_down_rect == null){
            mSign_down_rect = new Rect(0,0,0,0);
        }
        setMeasuredDimension(width,height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // save signed state as Picture
        if (mPicture_up == null){
            Log.d(TAG, "onDraw: sign up executed");
            mPicture_up = new Picture();
            Canvas canvas1 = mPicture_up.beginRecording(getWidth(),getHeight());
            for (int i = 1;i<itemCount*2;i++){
                if (i%2 == 0){
                    canvas1.drawLine(0,getHeight()-itemSize/2,itemSize,
                            getHeight()-itemSize/2,mPaint_signup);
                    canvas1.translate(itemSize,0);
                }else {
                    canvas1.drawBitmap(icon_sign_up,rect_src,rect_dest,null);
                    canvas1.translate(itemSize,0);
                }
            }
            mPicture_up.endRecording();
        }
        // save unsigned state as Picture
        if (mPicture_down == null){
            Log.d(TAG, "onDraw: sign down executed");
            mPicture_down = new Picture();
            Canvas canvas2 = mPicture_down.beginRecording(getWidth(),getHeight());
            for (int i = 1;i<itemCount*2;i++){
                if (i%2 == 0){
                    canvas2.drawLine(0,getHeight()-itemSize/2,itemSize,
                            getHeight()-itemSize/2,mPaint_signdown);
                    canvas2.translate(itemSize,0);
                }else {
                    canvas2.drawBitmap(icon_sign_down,rect_src,rect_dest,null);
                    if (itemText.length > 0){
                       String text = itemText[(i-1)/2];
                       mTextPaint.getTextBounds(text,0,text.length(),textBounds);
                       canvas2.drawText(text,rect_dest.width()-textBounds.width(),
                               rect_dest.top-textBounds.height()/2,mTextPaint);
                    }
                    if (i==7 || i == 13){
                        String s = "up";
                        mSpecialItemPaint.getTextBounds(s,0,s.length(),textBounds);
                        canvas2.drawText(s,(rect_dest.width()-textBounds.width())/2,
                                rect_dest.top-textBounds.height(),mSpecialItemPaint);
                    }
                    canvas2.translate(itemSize,0);
                }
            }
            mPicture_down.endRecording();
        }
        // draw unsigned state,judge not null to avoid redraw
        if (mPicture_down != null){
            Log.d(TAG, "onDraw:mPicture_down executed ");
            canvas.drawPicture(mPicture_down);
        }
        if (drawable == null){
            Log.d(TAG, "onDraw:mPicture_drawable executed ");
            drawable= new PictureDrawable(mPicture_up);
        }
        //init state if it's not the first sign
        if (signed_days > 0){
            if (signed_days == 1){
                mSign_down_rect.right = itemSize;
                mSign_down_rect.bottom = getHeight();
            }else {
                mSign_down_rect.right = itemSize*(2*signed_days-1);
                mSign_down_rect.bottom = getHeight();
            }
            signed_days = 0;
        }
        drawable.setBounds(mSign_down_rect);
        drawable.draw(canvas);
    }
    /**
     * This is called during layout when the size of this view has changed. If
     * you were just added to the view hierarchy, you're called with the old
     * values of 0.
     *
     * @param w Current width of this view.
     * @param h Current height of this view.
     * @param oldw Old width of this view.
     * @param oldh Old height of this view.
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    public void signUp(){
        int num_of_days = prefs.getInt("num_of_day",0);
        SharedPreferences.Editor editor;
        if (num_of_days < itemCount){
            if (num_of_days == 0){
                for (int right = 0;right<=itemSize;right +=1){
                    mSign_down_rect = new Rect(0,0,right,getHeight());
                    postInvalidateDelayed(100);
                }
            }else {
                for (int right=itemSize*(num_of_days*2-1);
                     right<=itemSize*(num_of_days*2+1);right+=1){
                    mSign_down_rect = new Rect(0,0,right,getHeight());
                    postInvalidateDelayed(100);
                }
            }
            num_of_days = num_of_days + 1;
            editor = prefs.edit();
            editor.putInt("num_of_day",num_of_days);
            editor.apply();
        }else{
            editor = prefs.edit();
            editor.putInt("num_of_day",0);
            editor.apply();
        }
    }

    public void setData(List<Object> list){
        if (list.size() != itemCount){
            throw new IllegalArgumentException("list size must be "+itemCount);
        }
        for (int i = 0;i<list.size();i++){
            itemText[i] = (String) list.get(i);
        }
    }
}
