package models;

import android.graphics.RectF;

public class Ball {
    RectF rect;
    private float xVelocity;
    private float yVelocity;
    public static float ballWidth = Paddle.screenHeight / 45;
    float ballHeight = ballWidth;
    float x1 = Paddle.screenHeight / 3;
    float y1 = Paddle.screenHeight / 1.5f;
    float x2 = x1 * 1.5f;
    float y2 = y1 * 0.75f;


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
            xVelocity = x2;
            if (yVelocity > 0)
                yVelocity = y2;
            else yVelocity = -y2;
        } else {
            xVelocity = x1;
            if (yVelocity > 0)
                yVelocity = y1;
            else yVelocity = -y1;
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
        xVelocity = x1;
        yVelocity = -y1;
    }

    public float getMidValue() {
        return (rect.right + rect.left) / 2;
    }
}