package com.ft_hangouts.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class StarBackgroundView extends View {
    private Paint starPaint;
    private List<Star> stars;
    private int starColor;
    private Random random;
    
    private static class Star {
        float x, y, size, rotation;
        
        Star(float x, float y, float size, float rotation) {
            this.x = x;
            this.y = y;
            this.size = size;
            this.rotation = rotation;
        }
    }
    
    public StarBackgroundView(Context context) {
        super(context);
        init();
    }
    
    public StarBackgroundView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public StarBackgroundView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        starPaint = new Paint();
        starPaint.setStyle(Paint.Style.STROKE);
        starPaint.setStrokeWidth(3f);
        starPaint.setAntiAlias(true);
        
        stars = new ArrayList<>();
        random = new Random();
    }
    
    public void setStarColor(int color) {
        this.starColor = color;
        starPaint.setColor(color);
        starPaint.setAlpha(100);
        invalidate();
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        generateStars(w, h);
    }
    
    private void generateStars(int width, int height) {
        stars.clear();
        if (width <= 0 || height <= 0) return;
        
        int numStars = 25;
        
        for (int i = 0; i < numStars; i++) {
            float x = random.nextFloat() * width;
            float y = random.nextFloat() * height;
            float size = 30 + random.nextFloat() * 50;
            float rotation = random.nextFloat() * 360;
            stars.add(new Star(x, y, size, rotation));
        }
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        for (Star star : stars) {
            drawStar(canvas, star.x, star.y, star.size, star.rotation);
        }
    }
    
    private void drawStar(Canvas canvas, float centerX, float centerY, float size, float rotation) {
        Path path = new Path();
        
        float outerRadius = size / 2;
        float innerRadius = outerRadius * 0.4f;
        
        canvas.save();
        canvas.rotate(rotation, centerX, centerY);
        
        for (int i = 0; i < 5; i++) {
            float outerAngle = (float) (Math.PI * 2 * i / 5 - Math.PI / 2);
            float innerAngle = (float) (Math.PI * 2 * (i + 0.5) / 5 - Math.PI / 2);
            
            float outerX = centerX + (float) Math.cos(outerAngle) * outerRadius;
            float outerY = centerY + (float) Math.sin(outerAngle) * outerRadius;
            
            float innerX = centerX + (float) Math.cos(innerAngle) * innerRadius;
            float innerY = centerY + (float) Math.sin(innerAngle) * innerRadius;
            
            if (i == 0) {
                path.moveTo(outerX, outerY);
            } else {
                path.lineTo(outerX, outerY);
            }
            path.lineTo(innerX, innerY);
        }
        
        path.close();
        canvas.drawPath(path, starPaint);
        canvas.restore();
    }
}