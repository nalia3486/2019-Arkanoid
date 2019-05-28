package com.example.nati.arkanoid;

import android.graphics.RectF;

import java.util.Random;

public class Ball {
    RectF rect;
    private float xVelocity;
    private float yVelocity;
    private float ballWidth = 20;
    private float ballHeight = 20;

    public Ball() {
        // Start the ball travelling straight up at 100 pixels per second
        xVelocity = 200;
        yVelocity = -400;
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

    public void setXVelocity(float paddle_mid, float hit_place, float length){
        float from_mid_distance = Math.abs(hit_place - paddle_mid);

//        if (!(from_mid_distance <length/4)) {
//            xVelocity=300;
//            if (yVelocity>0)
//            yVelocity=300;
//            else yVelocity=-300;
//        }
//        else{
//            xVelocity=200;
//            if (yVelocity>0)
//                yVelocity=400;
//            else yVelocity=-400;
//        }
        if (hit_place < paddle_mid){
            if (xVelocity > 0)
                reverseXVelocity();
        }
        else if (xVelocity < 0) reverseXVelocity();
    }

    public void clearObstacleY(float y) {
        rect.bottom = y;
        rect.top = y - ballHeight;
    }

    public void clearObstacleX(float x) {
        rect.left = x;
        rect.right = x + ballWidth;
    }

    public void reset(int x, int y) {
        rect.left = x / 2;
        rect.top = y - 40;
        rect.right = x / 2 + ballWidth;
        rect.bottom = y - 40 - ballHeight;
    }

    public float getMidValue(){
        return (rect.right + rect.left)/2;
    }
}