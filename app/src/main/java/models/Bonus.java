package models;

import android.graphics.RectF;

public class Bonus {
    private RectF rect;
    private float length;
    private float yVelocity;
    public int type;

    public Bonus(RectF brickRect){
        length = (brickRect.right - brickRect.left) / 3;
        rect = new RectF(brickRect.left + length, brickRect.top / 4, brickRect.right - length, brickRect.bottom / 4);
        yVelocity = 200;
    }

    public RectF getRect() {
        return this.rect;
    }

    public void update(long fps) {
        rect.top = rect.top + (yVelocity / fps);
        rect.bottom = rect.top - length + (yVelocity / fps);
    }

}
