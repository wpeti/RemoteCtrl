package com.wpeti.remotectrl;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * Created by wpeti on 2016.02.05..
 */
public class TouchFrameLayout extends FrameLayout {
    private Point _point = new Point(0,0);
    //new Point((int)(this.getWidth()/2),(int)(this.getHeight()/2))

    public TouchFrameLayout(Context context) {
        super(context);
    }

    public TouchFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TouchFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public TouchFrameLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        Paint p = new Paint();
        p.setColor(Color.YELLOW);
        super.onDraw(canvas);
        canvas.drawCircle(_point.x, _point.y, 60, p);
    }

    public void setCircleCoordinates(Point point){
        _point = point;
        this.invalidate();
        this.requestLayout();
    }
}
