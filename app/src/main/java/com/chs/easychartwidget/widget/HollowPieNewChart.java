package com.chs.easychartwidget.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.chs.easychartwidget.entity.PieDataEntity;
import com.chs.easychartwidget.utils.CalculateUtil;
import com.chs.easychartwidget.utils.DensityUtil;

import java.util.Arrays;
import java.util.List;

/**
 * 作者：chs on 2016/9/8 16:25
 * 邮箱：657083984@qq.com
 * 空心的饼状图表
 */
public class HollowPieNewChart extends View {
    public static final int TOUCH_OFFSET = 16;

    private int mTotalWidth, mTotalHeight;
    private float mOutRadius;

    private Paint mPaint,mLinePaint,mTextPaint;

    /**
     * 扇形的绘制区域
     */
    private RectF mOutRectF;
    /**
     * 点击之后的扇形的绘制区域
     */
    private RectF mRectFTouch;

    private List<PieDataEntity> mDataList;
    /**
     * 所有的数据加起来的总值
     */
    private float mTotalValue;
    /**
     * 扇形角度集合
     */
    private float[] angles;
    /**
     * 手点击的部分的position
     */
    private int position = -1;
    /**
     * 点击监听
     */
    private OnItemPieClickListener mOnItemPieClickListener;
    /**
     * 点击某一块之后再次点击回复原状
     */
    private int lastClickedPosition = -1;
    private boolean lastPositionClicked = false;
    public void setOnItemPieClickListener(OnItemPieClickListener onItemPieClickListener) {
        mOnItemPieClickListener = onItemPieClickListener;
    }

    public interface OnItemPieClickListener {
        void onClick(int position);
    }
    public HollowPieNewChart(Context context) {
        super(context);
        init(context);
    }

    public HollowPieNewChart(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public HollowPieNewChart(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mOutRectF = new RectF();
        mRectFTouch = new RectF();

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);

        mLinePaint = new Paint();
        mLinePaint.setAntiAlias(true);
        mLinePaint.setStyle(Paint.Style.FILL);
        mLinePaint.setStrokeWidth(2);
        mLinePaint.setColor(Color.BLACK);

        mTextPaint = new Paint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setTextSize(24);

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mTotalWidth = w - getPaddingLeft() - getPaddingRight();
        mTotalHeight = h - getPaddingTop() - getPaddingBottom();

        mOutRadius = (float) (Math.min(mTotalWidth,mTotalHeight)/2*0.7);

        mOutRectF.left = -mOutRadius;
        mOutRectF.top = -mOutRadius;
        mOutRectF.right = mOutRadius;
        mOutRectF.bottom = mOutRadius;

        mRectFTouch.left = -mOutRadius-TOUCH_OFFSET;
        mRectFTouch.top = -mOutRadius-TOUCH_OFFSET;
        mRectFTouch.right = mOutRadius+TOUCH_OFFSET;
        mRectFTouch.bottom = mOutRadius+TOUCH_OFFSET;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(mDataList==null)
            return;
        canvas.translate(mTotalWidth/2,mTotalHeight/2);
        //绘制饼图的每块区域
        drawPiePath(canvas);
    }

    /**
     * 绘制饼图的每块区域 和文本
     * @param canvas
     */
    private void drawPiePath(Canvas canvas) {
        //起始地角度
        float startAngle = 0;
        for(int i = 0;i<mDataList.size();i++){
            float sweepAngle = mDataList.get(i).getValue()/mTotalValue*360-1;//每个扇形的角度
//            mPath.moveTo(0,0);
            int xOffset = DensityUtil.dip2px(getContext(),10);
            mPaint.setColor(mDataList.get(i).getColor());
            mLinePaint.setColor(mDataList.get(i).getColor());
            mTextPaint.setColor(mDataList.get(i).getColor());

            if (position == i) {
                if (lastClickedPosition == position && lastPositionClicked) {
                    canvas.drawArc(mRectFTouch, startAngle, sweepAngle, true, mPaint);
                } else {
                    canvas.drawArc(mOutRectF, startAngle, sweepAngle, true, mPaint);
                }
            } else {
                canvas.drawArc(mOutRectF, startAngle, sweepAngle, true, mPaint);
            }
            angles[i] = sweepAngle;
//            if(position-1==i){
//                canvas.drawArc(mRectFTouch,startAngle,sweepAngle,true,mPaint);
//            }else {
//                canvas.drawArc(mOutRectF,startAngle,sweepAngle,true,mPaint);
//            }
//            canvas.drawPath(mPath,mPaint);
            Log.i("toRadians",(startAngle+sweepAngle/2)+"****"+Math.toRadians(startAngle+sweepAngle/2));
            //确定直线的起始和结束的点的位置
            float pxs = (float) (mOutRadius*Math.cos(Math.toRadians(startAngle+sweepAngle/2)));
            float pys = (float) (mOutRadius*Math.sin(Math.toRadians(startAngle+sweepAngle/2)));
            float pxt = (float) ((mOutRadius+xOffset)*Math.cos(Math.toRadians(startAngle+sweepAngle/2)));
            float pyt = (float) ((mOutRadius+xOffset)*Math.sin(Math.toRadians(startAngle+sweepAngle/2)));
            startAngle += sweepAngle+1;
            //绘制线和文本
            canvas.drawLine(pxs,pys,pxt,pyt,mLinePaint);
            float res = mDataList.get(i).getValue() / mTotalValue * 100;
            //提供精确的小数位四舍五入处理。
            double resToRound = CalculateUtil.round(res,2);
            if (startAngle % 360.0 >= 90.0 && startAngle % 360.0 <= 270.0) {
                canvas.drawLine(pxt,pyt,pxt-xOffset,pyt,mLinePaint);
                canvas.drawText(resToRound+"%",pxt-mTextPaint.measureText(resToRound+"%")-xOffset,pyt,mTextPaint);
            }else {
                canvas.drawLine(pxt,pyt,pxt+xOffset,pyt,mLinePaint);
                canvas.drawText(resToRound+"%",pxt+30,pyt,mTextPaint);
            }
        }
        mPaint.setColor(Color.WHITE);
        mPaint.setAlpha(40);
        canvas.drawCircle(0, 0, mOutRadius / 2 + DensityUtil.dip2px(getContext(),10), mPaint);
        mPaint.setAlpha(255);
        canvas.drawCircle(0, 0, mOutRadius / 2, mPaint);
    }

    public void setDataList(List<PieDataEntity> dataList){
        this.mDataList = dataList;
        mTotalValue = 0;
        for(PieDataEntity pieData :mDataList){
            mTotalValue +=pieData.getValue();
        }
        angles = new float[mDataList.size()];
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                float x = event.getX()-(mTotalWidth/2f);
                float y = event.getY()-(mTotalHeight/2f);
                //计算出角度
                float touchAngle = (float) Math.toDegrees(Math.atan2(x,y));
                if(x>0&&y>0){
                    touchAngle =90 - touchAngle;
                }else if(x<0&&y>0){
                    touchAngle = 90 - touchAngle;
                }else if(x<0&&y<0){
                    touchAngle = 90 - touchAngle;
                }else if(x>0&&y<0){
                    touchAngle +=180;
                }
                float touchRadius = (float) Math.sqrt(y * y + x * x);
                if (touchRadius< mOutRadius){
                    if(angles!=null)
                        position = getClickPosition(touchAngle);
                    if(lastClickedPosition == position){
                        lastPositionClicked = !lastPositionClicked;
                    }else {
                        lastPositionClicked = true;
                        lastClickedPosition = position;
                    }
                    invalidate();
                    if(mOnItemPieClickListener!=null){
                        mOnItemPieClickListener.onClick(position);
                    }
                }
                break;
                default:
        }
        return super.onTouchEvent(event);
    }

    private int getClickPosition(float touchAngle) {
        int position = 0;
        int totalAngle = 0;
        for (int i = 0; i < angles.length; i++) {
            totalAngle += angles[i];
            if (touchAngle <= totalAngle) {
                position = i;
                break;
            }
        }
        return position;
    }
}
