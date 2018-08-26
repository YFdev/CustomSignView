# CustomSignView
看到郭神公众号推送了一条签到的自定义view，一时兴起，决定自己做一个，效果图如下（没有动图，但是有动画效果的），做的比较粗糙，主要是巩固一下自定义view的知识。动态效果的实现没有用动画，主要原理是canvas.drawBitmap(Bitmap bitmap,Rect src,Rect dest,Paint paint)方法，即通过改变src实现绘制区域的改变，Canvas用法参考大神博客：https://blog.csdn.net/z_x_Qiang/article/details/76587328；

public class CustomView extends View {

    private static final String TAG = "CustomView";
    private Paint mPaint_signup,mPaint_signdown,mTextPaint,mSpecialItemPaint;
    //自定义总签到天数，即图标个数
    private int itemCount;
    //从xml中获取用户自定义图标
    private int sign_upId,sign_downId;
    //右角标文字
    private String[] itemText;
    //右角标文字大小
    private float itemTextSize;
    //设置裁剪区域和绘制区域，测量文字
    private Rect rect_src,rect_dest,mSign_down_rect,textBounds;
    //签到图标大小
    private int itemSize;
    //默认签到图标大小wrapContent（）
    private final int DEFAULT_ITEM_SIZE = 20;
    //已签到和未签到的状态图片
    private Bitmap icon_sign_up,icon_sign_down;
    //关键点：用Pictrue保存绘制过程
    private Picture mPicture_up,mPicture_down;
    private PictureDrawable drawable;
    //用于保存已签到天数
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
        //itemText = ta.getString(R.styleable.CustomView_item_text);
        itemTextSize = ta.getDimension(R.styleable.CustomView_item_textSize,8);
        ta.recycle();
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        init();
    }

    private void init() {
    //绘制图标之间的连线
        mPaint_signup = new Paint();
        mPaint_signup.setColor(Color.GREEN);
        mPaint_signup.setStrokeWidth(2.0f);
        mPaint_signup.setAntiAlias(true);
        
        mPaint_signdown = new Paint();
        mPaint_signdown.setColor(Color.GRAY);
        mPaint_signdown.setStrokeWidth(2.0f);
        mPaint_signdown.setAntiAlias(true);
    //绘制右角标文字
        mTextPaint = new Paint();
        mTextPaint.setColor(Color.GRAY);
        mTextPaint.setTextSize(itemTextSize);
    //绘制特殊需求文字，如up

        mSpecialItemPaint = new Paint();
        mSpecialItemPaint.setColor(Color.RED);
        mSpecialItemPaint.setTextSize(itemTextSize+4);
        textBounds = new Rect();
        //test data
        itemText = new String[itemCount];
        //测试数据
        
        for (int i=0;i<itemCount;i++){
            itemText[i] = "+"+i;
        }

        signed_days = prefs.getInt("num_of_day",0);

        icon_sign_up = BitmapFactory.decodeResource(getResources(),sign_upId);
        icon_sign_down = BitmapFactory.decodeResource(getResources(),sign_downId);
        rect_src = new Rect(0,0,icon_sign_up.getWidth(),icon_sign_up.getHeight());
        //禁用硬件加速，因为使用Picture
        setLayerType(View.LAYER_TYPE_HARDWARE,null);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    //   super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = MeasureSpec.getSize(widthMeasureSpec)-getPaddingLeft()-getPaddingRight();
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec)-getPaddingBottom()-getPaddingTop();
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        //用户设置wrapconent时给定默认大小
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
        //用picture录制完整的签到状态的图片，只保存，并不会绘制在屏幕上，先判空可以防止postinvalidate时重新绘制，提高性能
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
        //用picture录制完整的未签到状态的图片，只保存，并不会绘制在屏幕上，先判空可以防止postinvalidate时重新绘制，提高性能
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
                    //绘制文字
                    if (itemText.length > 0){
                       String text = itemText[(i-1)/2];
                       mTextPaint.getTextBounds(text,0,text.length(),textBounds);
                       canvas2.drawText(text,rect_dest.width()-textBounds.width(),
                               rect_dest.top-textBounds.height()/2,mTextPaint);
                    }
                    //绘制特殊要求字符，没仔细实现，所以没提供对外方法
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
        //真正绘制，先绘制完整图标（当然是未签到状态）
        if (mPicture_down != null){
            Log.d(TAG, "onDraw:mPicture_down executed ");
            canvas.drawPicture(mPicture_down);
        }
        //用PictureDrawable，可以实现裁剪区域绘制
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
        //绘制签到状态，初始时mSign_down_rect的L,T,R,B均为0，所以图标不可见，之后签到时我们操作这个rect就可以了
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
    //对外提供一个签到的方法
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
    //对外提供设置右角标文字的方法
    public void setData(List<Object> list){
        if (list.size() != itemCount){
            throw new IllegalArgumentException("list size must be "+itemCount);
        }
        for (int i = 0;i<list.size();i++){
            itemText[i] = (String) list.get(i);
        }
    }
}
![image](https://github.com/YFdev/CustomSignView/blob/master/images/p1.PNG)
![image](https://github.com/YFdev/CustomSignView/blob/master/images/p2.PNG)
