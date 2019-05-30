package com.example.nati.arkanoid;

import android.graphics.RectF;

public class Ball {
    RectF rect;
    private float xVelocity;
    private float yVelocity;
    static float ballWidth = 20;
    float ballHeight = 20;

    public Ball() {
        // Start the ball travelling straight up at 100 pixels per second
        xVelocity = 200;
        yVelocity = 400;
        rect = new RectF();
    }

    public RectF getRect() {
        return rect;
    }

    public void update(long fps) {
        rect.left = rect.left + (xVelocity / fps);
        rect.top = rect.top + (yVelocity / fps);
        rect.right = rect.left + ballWidth;
        rect.bottom = rect.top - ballHeight;
    }

    public void reverseYVelocity() {
        yVelocity = -yVelocity;
    }

    public void reverseXVelocity() {
        xVelocity = -xVelocity;
    }

    public void setXVelocity(float paddle_mid, float hit_place, float length) {
        float from_mid_distance = Math.abs(hit_place - paddle_mid);

        if (!(from_mid_distance < length / 4)) {
            xVelocity = 300;
            if (yVelocity > 0)
                yVelocity = 300;
            else yVelocity = -300;
        } else {
            xVelocity = 200;
            if (yVelocity > 0)
                yVelocity = 400;
            else yVelocity = -400;
        }
        if (hit_place < paddle_mid) {
            reverseXVelocity();
        }
    }

    public void clearObstacleY(float y) {
        rect.bottom = y;
        rect.top = y;
    }

    public void clearObstacleX(float x) {
        rect.left = x;
        rect.right = x;
    }

    public void reset(int x, int y) {
        rect.left = x / 2;
        rect.top = y * 0.75f;
        rect.right = x / 2 + ballWidth;
        rect.bottom = y * 0.75f - ballHeight;
        xVelocity = 200;
        yVelocity = -400;
    }

    public float getMidValue() {
        return (rect.right + rect.left) / 2;
    }
}