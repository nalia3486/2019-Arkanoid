package com.example.nati.arkanoid;

import android.graphics.RectF;

public class Paddle {
    // RectF have four coordinates
    private RectF rect;
    private float length;
    private float height;

    // X is the far left of the rectangle which forms our paddle
    private float x;

    // Y is the top coordinate
    private float y;

    // This will hold the pixels per second speed of the paddle
    private float paddleSpeed;

    // Which ways can the paddle move
    public final int STOPPED = 0; //zrobic tutaj enuma
    public final int LEFT = 1;
    public final int RIGHT = 2;

    // If our paddle is moving and where
    private int paddleMoving = STOPPED;

    public Paddle(int screenX, int screenY) {
        length = screenX / 6;
        height = screenY / 6;

        x = screenX / 2 - length / 2;
        y = screenY - 150;

        rect = new RectF(x, y, x + length, y + height);
        paddleSpeed = 350;
    }

    public RectF getRect() {
        return rect;
    }

    public void setMovementState(int state) {
        paddleMoving = state;
    }

    public void update(long fps) {
        if (paddleMoving == LEFT) {
            x = x - paddleSpeed / fps;
        }

        if (paddleMoving == RIGHT) {
            x = x + paddleSpeed / fps;
        }

        rect.left = x;
        rect.right = x + length;
    }

    public void reset(float screenX,float screenY){
        rect.left = screenX / 2-length/2;
        rect.top =  screenY-250;
        rect.right=rect.left+length;
        rect.bottom=rect.top+length;
    }
}