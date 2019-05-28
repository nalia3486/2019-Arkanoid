package com.example.nati.arkanoid;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

public class MainActivity extends Activity {

    ArkanoidView arkanoidView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize view
        arkanoidView = new ArkanoidView(this);
        setContentView(arkanoidView);
    }

    class ArkanoidView extends SurfaceView implements Runnable {

        // This is our thread
        Thread gameThread = null;
        SurfaceHolder surfaceHolder;

        // when the playGame is running or not
        volatile boolean playGame;

        // Game is paused at the start, we have to move
        boolean paused = true;

        Canvas canvas;
        Paint paint;

        // Tracks the game frame rate
        long fps;

        // calculate the fps
        private long timeThisFrame;

        // The size of the screen in pixels
        int screenX;
        int screenY;

        Brick[] bricks = new Brick[200];
        int numBricks = 0;

        SoundPool soundPool;
        int beep1ID = -1;
        int beep2ID = -1;
        int beep3ID = -1;
        int loseLifeID = -1;
        int explodeID = -1;
        int score = 0;
        int lives = 3;

        Ball ball;
        Paddle paddle;

        // We want to see screen details
        Display display = getWindowManager().getDefaultDisplay();

        // Load the resolution into a Point object
        Point size = new Point();

        int level = 1;
        boolean endGame = false;

        public ArkanoidView(Context context) {
            super(context);

            surfaceHolder = getHolder();
            paint = new Paint();

            display.getSize(size);

            screenX = size.x;
            screenY = size.y;

            paddle = new Paddle(screenX, screenY);
            ball = new Ball(screenX, screenY);

            soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
            try {
                AssetManager assetManager = context.getAssets();
                AssetFileDescriptor descriptor;

                descriptor = assetManager.openFd("beep1.ogg");
                beep1ID = soundPool.load(descriptor, 0);

                descriptor = assetManager.openFd("beep2.ogg");
                beep2ID = soundPool.load(descriptor, 0);

                descriptor = assetManager.openFd("beep3.ogg");
                beep3ID = soundPool.load(descriptor, 0);

                descriptor = assetManager.openFd("loseLife.ogg");
                loseLifeID = soundPool.load(descriptor, 0);

                descriptor = assetManager.openFd("explode.ogg");
                explodeID = soundPool.load(descriptor, 0);

            } catch (IOException e) {
                Log.e("error", "Failed to load sound!");
            }
            createBricksAndRestart();
        }

        public void createBricksAndRestart() {
            ball.reset(screenX, screenY);
            paddle = new Paddle(screenX, screenY);

            int brickWidth = screenX / 8;
            int brickHeight = screenY / 10;

            // We build bricks here
            numBricks = 0;
            for (int column = 0; column < 8; column++) {
                for (int row = 0; row < 3; row++) {
                    bricks[numBricks] = new Brick(row, column, brickWidth, brickHeight);
                    numBricks++;
                }
            }
            // if game over reset scores and lives
            //dodać tutaj przycisk startu?
            if (lives == 0) {
                score = 0;
                lives = 3;
                level = 1;
            }
        }

        @Override
        public void run() {
            while (playGame) {
                if(!endGame) {
                    long startFrameTime = System.currentTimeMillis();
                    if (!paused) {
                        update();
                    }
                    draw();

                    timeThisFrame = System.currentTimeMillis() - startFrameTime;
                    if (timeThisFrame >= 1) {
                        fps = 1000 / timeThisFrame;
                    }
                }
            }
        }

        public void update() {
            paddle.update(fps);
            ball.update(fps);

            // Check for ball colliding with a brick
            for (int i = 0; i < numBricks; i++) {
                if (bricks[i].getVisibility()) {
                    if (RectF.intersects(bricks[i].getRect(), ball.getRect())) {
                        bricks[i].setInvisible();
                        ball.reverseYVelocity();
                        score = score + 10;
                        soundPool.play(explodeID, 1, 1, 0, 0, 1);
                    }
                }
            }
            // Check for ball colliding with paddle
            if (RectF.intersects(paddle.getRect(), ball.getRect())) {
                ball.setRandomXVelocity();
                ball.reverseYVelocity();
                ball.clearObstacleY(paddle.getRect().top - 2);
                soundPool.play(beep1ID, 1, 1, 0, 0, 1);
            }
            // Bounce the ball back when it hits the bottom of screen
            if (ball.getRect().bottom + paddle.getHeight()/3> screenY) {
                ball.reverseYVelocity();
                ball.clearObstacleY(screenY - 2);

                //paddle.getX(screenX);

                lives--;
                soundPool.play(loseLifeID, 1, 1, 0, 0, 1);

                ball.reset(screenX, screenY);
                paddle = new Paddle(screenX, screenY);

                if (lives == 0) {
                    paused = true;
                }
            }

            // Bounce the ball back when it hits the top of screen
            if (ball.getRect().top < 0) {
                ball.reverseYVelocity();
                ball.clearObstacleY(24);
                soundPool.play(beep2ID, 1, 1, 0, 0, 1);
            }

            // If the ball hits left wall bounce
            if (ball.getRect().left < 0) {
                ball.reverseXVelocity();
                ball.clearObstacleX(2);
                soundPool.play(beep3ID, 1, 1, 0, 0, 1);
            }

            // If the ball hits right wall bounce
            if (ball.getRect().right > screenX - 20) {
                ball.reverseXVelocity();
                ball.clearObstacleX(screenX - 44);
                soundPool.play(beep3ID, 1, 1, 0, 0, 1);
            }
        }

        // Draw the newly updated scene
        public void draw() {
            if (surfaceHolder.getSurface().isValid()) {
                canvas = surfaceHolder.lockCanvas();

                canvas.drawColor(Color.argb(255, 144, 62, 182));
                paint.setColor(Color.argb(255, 255, 255, 255));
                canvas.drawRect(paddle.getRect(), paint);
                canvas.drawOval(ball.getRect(), paint);
                paint.setColor(Color.argb(255, 90, 200, 70));

                for (int i = 0; i < numBricks; i++) {
                    if (bricks[i].getVisibility()) {
                        canvas.drawRect(bricks[i].getRect(), paint);
                    }
                }

                paint.setColor(Color.argb(255, 255, 255, 255));
                paint.setTextSize(70);
                canvas.drawText("Level: " + level + "   Score: " + score + "   Live: " + lives, 10, 50, paint);

                // Won
                if (winLevel()) {
                    paint.setTextSize(90);
                    paused = true;
                    level++;
                    if (level==2){
                        endGame= true;
                        canvas.drawText("WYGRANA!", 10, screenY / 2, paint);
                    }
                    else{
                        createBricksAndRestart();
                    }
                }

                // Lost
                else if (lives <1 ) {
                    paint.setTextSize(90);
                    paused = true;
                    endGame= true;
                    canvas.drawText("PRZEGRANA!", 10, screenY / 2, paint);
                }

                surfaceHolder.unlockCanvasAndPost(canvas);
            }
        }

        private boolean winLevel() {
            for (int i=0; i< numBricks; i++){
                if (bricks[i].getVisibility()) {
                    return false;
                }
            }
            return true;
        }

        public void pause() {
            playGame = false;
            try {
                gameThread.join();
            } catch (InterruptedException e) {
                Log.e("Error:", "Joining thread!");
            }
        }

        public void resume() {
            playGame = true;
            gameThread = new Thread(this);
            gameThread.start();
        }

        @Override
        public boolean onTouchEvent(MotionEvent motionEvent) {
            switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    paused = false;
                    if (endGame) {
                        endGame=false;
                        playGame=true;
                        paused = false;
                        createBricksAndRestart();
                    }
                    if (motionEvent.getX() > screenX / 2)
                        paddle.setMovementState(paddle.RIGHT);
                    else paddle.setMovementState(paddle.LEFT);
                    break;
                case MotionEvent.ACTION_UP:
                    paddle.setMovementState(paddle.STOPPED);
                    break;
            }
            return true;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        arkanoidView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        arkanoidView.pause();
    }
}