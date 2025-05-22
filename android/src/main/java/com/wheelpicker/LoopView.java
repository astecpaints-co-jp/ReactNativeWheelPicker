package com.wheelpicker;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;


public class LoopView extends View {
    ScheduledExecutorService mExecutor = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> mFuture;
    int totalScrollY;
    Handler handler;
    LoopListener loopListener;
    private GestureDetector gestureDetector;
    private int selectedItem;
    private GestureDetector.SimpleOnGestureListener simpleOnGestureListener;
    Context context;
    Paint paintA;  //paint that draw top and bottom text
    Paint paintB;  // paint that draw center text
    Paint paintC;  // paint that draw line besides center text
    List<WheelItem> items;
    int textSize;
    int maxTextWidth;
    int maxTextHeight;
    int colorGray;
    int colorBlack;
    int colorGrayLight;
    int colorDisabled;
    float lineSpacingMultiplier;
    boolean isLoop;
    int firstLineY;
    int secondLineY;
    int preCurrentIndex;
    int initPosition;
    int itemCount;
    int measuredHeight;
    int halfCircumference;
    int radius;
    int measuredWidth;
    int change;
    float y1;
    float y2;
    float dy;

    public LoopView(Context context) {
        super(context);
        initLoopView(context);
    }

    public LoopView(Context context, AttributeSet attributeset) {
        super(context, attributeset);
        initLoopView(context);
    }

    public LoopView(Context context, AttributeSet attributeset, int defStyleAttr) {
        super(context, attributeset, defStyleAttr);
        initLoopView(context);
    }

    private void initLoopView(Context context) {
        textSize = 0;
        colorGray = 0xffafafaf;
        colorBlack = 0xff313131;
        colorGrayLight = 0xffc5c5c5;
        colorDisabled = 0x40cccccc;
        lineSpacingMultiplier = 2.0F;
        isLoop = false;
        initPosition = 0;
        itemCount = 7;
        y1 = 0.0F;
        y2 = 0.0F;
        dy = 0.0F;
        totalScrollY = 0;
        simpleOnGestureListener = new LoopViewGestureListener(this);
        handler = new MessageHandler(this);
        this.context = context;
        setTextSize(16F);

        paintA = new Paint();
        paintA.setColor(colorGrayLight);
        paintB = new Paint();
        paintB.setTextSize(textSize);
        paintC = new Paint();
        paintA.setTextSize(textSize);
        if (android.os.Build.VERSION.SDK_INT >= 11) {
            setLayerType(LAYER_TYPE_SOFTWARE, null);
        }
        gestureDetector = new GestureDetector(context, simpleOnGestureListener);
        gestureDetector.setIsLongpressEnabled(false);
    }

    static int getSelectedItem(LoopView loopview) {
        return loopview.selectedItem;
    }

    static void smoothScroll(LoopView loopview) {
        loopview.smoothScroll();
    }

    private void initData() {
        if (items == null) {
            return;
        }
        paintA.setAntiAlias(true);
        paintB.setAntiAlias(true);
        paintC.setAntiAlias(true);
        paintC.setTypeface(Typeface.MONOSPACE);
        paintC.setTextSize(textSize);
        measureTextWidthHeight();
        halfCircumference = (int) (maxTextHeight * lineSpacingMultiplier * (itemCount - 1));
        measuredHeight = (int) ((halfCircumference * 2) / Math.PI);
        radius = (int) (halfCircumference / Math.PI);
        firstLineY = (int) ((measuredHeight - lineSpacingMultiplier * maxTextHeight) / 2.0F);
        secondLineY = (int) ((measuredHeight + lineSpacingMultiplier * maxTextHeight) / 2.0F);
        if (initPosition == -1) {
            if (isLoop) {
                initPosition = (items.size() + 1) / 2;
            } else {
                initPosition = 0;
            }
        }
        preCurrentIndex = initPosition;
    }

    private void measureTextWidthHeight() {
        Rect rect = new Rect();
        for (int i = 0; i < items.size(); i++) {
            String label = items.get(i).getLabel();
            paintB.getTextBounds(label, 0, label.length(), rect);
            int textWidth = rect.width();
            if (textWidth > maxTextWidth) {
                maxTextWidth = textWidth;
            }
            paintB.getTextBounds("\u661F\u671F", 0, 2, rect); // 星期
            int textHeight = rect.height();
            if (textHeight > maxTextHeight) {
                maxTextHeight = textHeight;
            }
        }

    }


    private void smoothScroll() {
        int offset = (int) (totalScrollY % (lineSpacingMultiplier * maxTextHeight));
        cancelFuture();
        mFuture = mExecutor.scheduleWithFixedDelay(new MTimer(this, offset), 0, 10, TimeUnit.MILLISECONDS);
    }

    public void cancelFuture() {
        if (mFuture!=null&&!mFuture.isCancelled()) {
            mFuture.cancel(true);
            mFuture = null;
        }
    }

    public final int getSelectedItem() {
        return selectedItem;
    }

    protected final void smoothScroll(float velocityY) {
        cancelFuture();
        int velocityFling = 20;
        mFuture = mExecutor.scheduleWithFixedDelay(new LoopTimerTask(this, velocityY), 0, velocityFling, TimeUnit.MILLISECONDS);
    }


    protected final void itemSelected() {
        if (loopListener != null) {
            postDelayed(new LoopRunnable(this), 200L);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        String as[];
        if (items == null) {
            super.onDraw(canvas);
            return;
        }

        // to center loop vertically
        int offsetY = (this.getHeight() - measuredHeight) / 2 - 2;
        firstLineY = (int) ((measuredHeight - lineSpacingMultiplier * maxTextHeight) / 2.0F) + offsetY ;
        secondLineY = (int) ((measuredHeight + lineSpacingMultiplier * maxTextHeight) / 2.0F)  + offsetY;

        as = new String[itemCount];
        change = (int) (totalScrollY / (lineSpacingMultiplier * maxTextHeight));
        preCurrentIndex = initPosition + change % items.size();
        if (!isLoop) {
            if (preCurrentIndex < 0) {
                preCurrentIndex = 0;
            }
            if (preCurrentIndex > items.size() - 1) {
                preCurrentIndex = items.size() - 1;
            }
            // break;
        } else {
            if (preCurrentIndex < 0) {
                preCurrentIndex = items.size() + preCurrentIndex;
            }
            if (preCurrentIndex > items.size() - 1) {
                preCurrentIndex = preCurrentIndex - items.size();
            }
            // continue;
        }

        int j2 = (int) (totalScrollY % (lineSpacingMultiplier * maxTextHeight));
        int k1 = 0;
        while (k1 < itemCount) {
            int l1 = preCurrentIndex - (itemCount / 2 - k1);
            if (isLoop) {
                if (l1 < 0) {
                    l1 = l1 + items.size();
                }
                if (l1 > items.size() - 1) {
                    l1 = l1 - items.size();
                }
                as[k1] = items.get(l1).getLabel();
            } else if (l1 < 0) {
                as[k1] = "";
            } else if (l1 > items.size() - 1) {
                as[k1] = "";
            } else {
                as[k1] = items.get(l1).getLabel();
            }
            k1++;
        }
        canvas.drawLine(0.0F, firstLineY, measuredWidth, firstLineY, paintC);
        canvas.drawLine(0.0F, secondLineY, measuredWidth, secondLineY, paintC);
        int j1 = 0;
        while (j1 < itemCount) {
            canvas.save();
            // L=α* r
            // (L * π ) / (π * r)
            float itemHeight = maxTextHeight * lineSpacingMultiplier;
            double radian = ((itemHeight * j1 - j2) * Math.PI) / halfCircumference;
            float angle = (float) (90D - (radian / Math.PI) * 180D);
            if (angle >= 90F || angle <= -90F) {
                canvas.restore();
            } else {
                int translateY = (int) (radius - Math.cos(radian) * radius - (Math.sin(radian) * maxTextHeight) / 2D) + offsetY;
                canvas.translate(0.0F, translateY);
                canvas.scale(1.0F, (float) Math.sin(radian));
                
                int actualPosition = preCurrentIndex - (itemCount / 2 - j1);
                if (isLoop) {
                    if (actualPosition < 0) {
                        actualPosition += items.size();
                    }
                    if (actualPosition > items.size() - 1) {
                        actualPosition -= items.size();
                    }
                }
                
                boolean isDisabled = items.get(actualPosition).isDisabled();
                
                Paint currentPaintA = isDisabled ? getDisabledPaint(paintA) : paintA;
                Paint currentPaintB = isDisabled ? getDisabledPaint(paintB) : paintB;
                
                if (translateY <= firstLineY && maxTextHeight + translateY >= firstLineY) {
                    canvas.save();
                    //top = 0,left = (measuredWidth - maxTextWidth)/2
                    canvas.clipRect(0, 0, measuredWidth, firstLineY - translateY);
                    drawCenter(canvas, currentPaintA, as[j1],maxTextHeight);
                    canvas.restore();
                    canvas.save();
                    canvas.clipRect(0, firstLineY - translateY, measuredWidth, (int) (itemHeight));
                    drawCenter(canvas, currentPaintB, as[j1], maxTextHeight);
                    canvas.restore();
                } else if (translateY <= secondLineY && maxTextHeight + translateY >= secondLineY) {
                    canvas.save();
                    canvas.clipRect(0, 0, measuredWidth, secondLineY - translateY);
                    drawCenter(canvas, currentPaintB, as[j1], maxTextHeight);
                    canvas.restore();
                    canvas.save();
                    canvas.clipRect(0, secondLineY - translateY, measuredWidth, (int) (itemHeight));
                    drawCenter(canvas, currentPaintA, as[j1],maxTextHeight);
                    canvas.restore();
                } else if (translateY >= firstLineY && maxTextHeight + translateY <= secondLineY) {
                    canvas.clipRect(0, 0, measuredWidth, (int) (itemHeight));
                    drawCenter(canvas, currentPaintB, as[j1],maxTextHeight);
                    final String selectedLabel = as[j1];
                    selectedItem = firstIndex(items, x -> x.getLabel() == selectedLabel);
                } else {
                    canvas.clipRect(0, 0, measuredWidth, (int) (itemHeight));
                    drawCenter(canvas, currentPaintA, as[j1],maxTextHeight);
                }
                canvas.restore();
            }
            j1++;
        }
        super.onDraw(canvas);
    }

    public static <T> int firstIndex(List<T> list, Predicate<T> condition) {
        for (int i = 0; i < list.size(); i++) {
            if (condition.test(list.get(i))) {
                return i;
            }
        }
        return -1;
    }
    
    private Paint getDisabledPaint(Paint original) {
        Paint disabledPaint = new Paint(original);
        disabledPaint.setColor(colorDisabled);
        return disabledPaint;
    }

    private Rect r = new Rect();

    private void drawCenter(Canvas canvas, Paint paint, String text, int y) {
        canvas.getClipBounds(r);
        int cWidth = r.width();
        paint.setTextAlign(Paint.Align.LEFT);
        paint.getTextBounds(text, 0, text.length(), r);
        float x = cWidth / 2f - r.width() / 2f - r.left;
        canvas.drawText(text, x, y, paint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        initData();
        measuredWidth = getMeasuredWidth();
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionevent) {
        int enabledCount = 0;
        if (items != null) {
            for (WheelItem item : items) {
                if (!item.isDisabled()) enabledCount++;
            }
        }
        if (enabledCount <= 1) {
            return false;
        }

        switch (motionevent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                y1 = motionevent.getRawY();
                if (getParent() != null) {
                  getParent().requestDisallowInterceptTouchEvent(true);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                y2 = motionevent.getRawY();
                dy = y1 - y2;
                y1 = y2;
                totalScrollY = (int) ((float) totalScrollY + dy);
                if (!isLoop) {
                    int initPositionCircleLength = (int) (initPosition * (lineSpacingMultiplier * maxTextHeight));
                    int initPositionStartY = -1 * initPositionCircleLength;
                    if (totalScrollY < initPositionStartY) {
                        totalScrollY = initPositionStartY;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
            default:
                if (!gestureDetector.onTouchEvent(motionevent) && motionevent.getAction() == MotionEvent.ACTION_UP) {
                    smoothScroll();
                }
                if (getParent() != null) {
                  getParent().requestDisallowInterceptTouchEvent(false);
                }
                return true;
        }

        if (!isLoop) {
            int circleLength = (int) ((float) (items.size() - 1 - initPosition) * (lineSpacingMultiplier * maxTextHeight));
            if (totalScrollY >= circleLength) {
                totalScrollY = circleLength;
            }
        }
        invalidate();

        if (!gestureDetector.onTouchEvent(motionevent) && motionevent.getAction() == MotionEvent.ACTION_UP) {
            smoothScroll();
        }
        return true;
    }

    // Picker methods
    public final void setLoop(boolean isCyclic) {
        isLoop = isCyclic;
    }

    public final void setTextSize(float size) {
        if (size > 0.0F) {
            textSize = (int) (context.getResources().getDisplayMetrics().density * size);
        }
    }

    public final void setInitPosition(int initPosition) {
        this.initPosition = initPosition;
    }

    public final void setListener(LoopListener LoopListener) {
        loopListener = LoopListener;
    }

    public final void setItems(List<WheelItem> items) {
        this.items = items;
        initData();
        invalidate();
    }

    public final void setSelectedItemTextColor(int color) {
        paintB.setColor(color);
    }

    public final void setDisabledItemTextColor(int color) {
        colorDisabled = color;
        invalidate();
    }

    public final void setSelectedItemTextSize(int textSize) {
        float scaledSizeInPixels = textSize * getResources().getDisplayMetrics().scaledDensity;
        paintB.setTextSize(scaledSizeInPixels);
    }

    public final void setSelectedItemFont(Typeface font) {
        paintB.setTypeface(font);
    }

    public final void setItemTextColor(int color) {
        paintA.setColor(color);
    }

    public final void setItemTextSize(int textSize) {
        float scaledSizeInPixels = textSize * getResources().getDisplayMetrics().scaledDensity;
        paintA.setTextSize(scaledSizeInPixels);
    }

    public final void setItemFont(Typeface font) {
        paintA.setTypeface(font);
    }

    public final void setIndicatorColor(int color) {
        paintC.setColor(color);
    }

    public final void setIndicatorWidth(int width) {
        paintC.setStrokeWidth(width);
    }

    public final void hideIndicator() {
        paintC.setColor(Color.TRANSPARENT);
    }

    public final void setSelectedItem(int position) {
        totalScrollY = (int) ((float) (position - initPosition) * (lineSpacingMultiplier * maxTextHeight));
        invalidate();
        smoothScroll();
    }
    
    public boolean isItemDisabled(int position) {
        return position >= 0 &&
               position < items.size() && 
               items.get(position).isDisabled();
    }

    public int findNearestEnabled(int fromIndex) {
        if (dy == 0) {
            int up = fromIndex, down = fromIndex;
            while (up >= 0 || down < items.size()) {
                if (up >= 0 && !items.get(up).isDisabled()) return up;
                if (down < items.size() && !items.get(down).isDisabled()) return down;
                up--;
                down++;
            }
            return -1;
        }
        if (dy < 0) { // down
            for (int i = fromIndex + 1; i < items.size(); i++) {
                if (!items.get(i).isDisabled()) return i;
            }
            for (int i = fromIndex - 1; i >= 0; i--) {
                if (!items.get(i).isDisabled()) return i;
            }
        } else { // up
            for (int i = fromIndex - 1; i >= 0; i--) {
                if (!items.get(i).isDisabled()) return i;
            }
            for (int i = fromIndex + 1; i < items.size(); i++) {
                if (!items.get(i).isDisabled()) return i;
            }
        }
        return -1;
    }

}
