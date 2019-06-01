package activity;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.example.nati.arkanoid.R;

import java.io.IOException;
import java.util.Random;

import models.Ball;
import models.Brick;
import models.Paddle;

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
        int screenX;
        int screenY;

        MediaPlayer mp1 = MediaPlayer.create(MainActivity.this, R.raw.a1);
        MediaPlayer mp2 = MediaPlayer.create(MainActivity.this, R.raw.a2);
        MediaPlayer mp3 = MediaPlayer.create(MainActivity.this, R.raw.a3);
        MediaPlayer mp4 = MediaPlayer.create(MainActivity.this, R.raw.levelstart);

        Resources res = getResources();
        Bitmap bitmap = BitmapFactory.decodeResource(res, R.drawable.back1);

        Brick[] bricks = new Brick[25];
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

        //see screen details
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
            ball = new Ball();

            soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);

            beep1ID = music("a6.ogg", context);
            beep2ID = music("beep2.ogg", context);
            beep3ID = music("beep3.ogg", context);
            loseLifeID = music("loseLife.ogg", context);
            explodeID = music("explode.ogg", context);

            createBricks();
        }

        private void resetGame() {
                score = 0;
                lives = 3;
                level = 1;
            createBricks();
            mp4.start();
            bitmap = BitmapFactory.decodeResource(res, R.drawable.back1);
        }

        private int music(String s, Context context) {
            AssetManager assetManager = context.getAssets();
            AssetFileDescriptor descriptor = null;
            try {
                descriptor = assetManager.openFd(s);
            } catch (IOException e) {
                Log.e("error", "Failed to load sound!");
            }
            return soundPool.load(descriptor, 0);
        }

        public void createBricks() {
            ball.reset(screenX, screenY);
            paddle = new Paddle(screenX, screenY);

            int brickWidth = screenX;
            int brickHeight = screenY / 12;
            numBricks = 0;

            int maxColumn;
            int maxRow ;

            switch(level){
                case 1:
                   maxColumn = 8;
                   maxRow = 4;
                    for (int column = 0; column < maxColumn; column++) {
                        for (int row = 1; row < maxRow; row++) {
                            bricks[numBricks] = new Brick(row, column, brickWidth / 8, brickHeight);
                            if (bricks[numBricks].getRect().left > 0 && bricks[numBricks].getRect().right < screenX) {
                                bricks[numBricks].hits = maxRow - row;
                                numBricks++;
                            }
                        }
                    }
                    break;
                case 2:
                    maxColumn=7;
                    maxRow = 4;
                        for (int row = 0; row < maxRow; row++) {
                            for (int column = 0; column < maxColumn; column++) {
                            bricks[numBricks] = new Brick(row, column, brickWidth / 8, brickHeight);
                            if (bricks[numBricks].getRect().left > 0 && bricks[numBricks].getRect().right < screenX) {
                                bricks[numBricks].hits =column+1;
                                numBricks++;
                            }
                        }
                    maxColumn--;
                    }
                    break;
                default:
                    maxColumn = 8;
                    maxRow = 4;
                    Random rand = new Random();
                    for (int column = 0; column < maxColumn; column++) {
                        for (int row = 0; row < maxRow; row++) {
                            bricks[numBricks] = new Brick(row, column, brickWidth / 8, brickHeight);
                            if (bricks[numBricks].getRect().left > 0 && bricks[numBricks].getRect().right < screenX) {
                                int n = rand.nextInt(5);
                                bricks[numBricks].hits = n + 1;
                                numBricks++;
                            }
                        }
                    }
                    break;
            }
        }

        @Override
        public void run() {
            mp4.start();
            while (playGame) {
                if (!endGame) {
                    long startFrameTime = System.currentTimeMillis();
                    if (!paused) {
                        paddle.update(fps);
                        ball.update(fps);

                        collidingWithBrick();
                        try {
                            Thread.sleep(5);
                        } catch (InterruptedException ex) {
                            Thread.currentThread().interrupt();
                        }
                        collidingWithPaddle();
                        ballHitsBottomOfScreen();
                        ballHitsTopOfScreen();
                        ballHitsLeftWall();
                        ballHitsRightWall();
                    }
                    draw();
                    timeThisFrame = System.currentTimeMillis() - startFrameTime;
                    if (timeThisFrame >= 1) {
                        fps = 1000 / timeThisFrame;
                    }
                }
            }
        }

        private void ballHitsRightWall() {
            if (ball.getRect().right > screenX - 20) {
                ball.reverseXVelocity();
                ball.clearObstacleX(screenX - 44);
                //soundPool.play(beep3ID, 1, 1, 0, 0, 1);
            }
        }

        private void ballHitsLeftWall() {
            if (ball.getRect().left < 0) {
                ball.reverseXVelocity();
                ball.clearObstacleX(2);
                //soundPool.play(beep3ID, 1, 1, 0, 0, 1);
            }
        }

        private void ballHitsTopOfScreen() {
            if (ball.getRect().top < 0) {
                ball.reverseYVelocity();
                ball.clearObstacleY(24);
                //soundPool.play(beep2ID, 1, 1, 0, 0, 1);
            }
        }

        private void ballHitsBottomOfScreen() {
            if (ball.getRect().bottom + paddle.getHeight() / 3 > screenY) {
                ball.reverseYVelocity();
                ball.clearObstacleY(screenY - 2);

                lives--;
                //soundPool.play(loseLifeID, 1, 1, 0, 0, 1);
                mp3.start();

                ball.reset(screenX, screenY);
                paddle = new Paddle(screenX, screenY);
                paused = true;
            }
        }

        private void collidingWithPaddle() {
            if (intersects(paddle.getRect(), ball.getRect())) {
                float paddleMid = paddle.getMidValue();
                float ballMid = ball.getMidValue();
                ball.setXVelocity(paddleMid, ballMid, paddle.getLength());
                ball.reverseYVelocity();
                ball.clearObstacleY(paddle.getRect().top - 4);
                //soundPool.play(beep1ID, 1, 1, 0, 0, 1);
                mp1.start();
            }
        }

        public boolean intersects(RectF a, RectF b) {
            return a.left < b.right && b.left < a.right
                    && a.top < b.bottom + Ball.ballWidth && b.top < a.bottom + Ball.ballWidth;
        }

        public int hitBrickOnSide(RectF a, RectF b) {
            if (a.left < b.right && b.left < a.right)
                return 1;
            else return 0;
        }

        public int hitBrickOnBottom(RectF a, RectF b) {
            if (a.top < b.bottom + Ball.ballWidth && b.top < a.bottom + Ball.ballWidth)
                return 2;
            else return 0;
        }

        int[] hit_point = new int[24];

        private void collidingWithBrick() {
            for (int i = 0; i < numBricks; i++) {
                if (bricks[i].getVisibility()) {
                    if (intersects(bricks[i].getRect(), ball.getRect())) {
                        bricks[i].hits--;
                        if (bricks[i].hits == 0) {
                            bricks[i].setInvisible();
                        } else {
                            addColorToBricks(i);
                        }
                        //tego ifa ponizej mozna wywalic i bd dzilalao jak poprzednio
                        if (hit_point[i] == 2) {
                            ball.reverseXVelocity();
                        } else {
                            ball.reverseYVelocity();
                        }
                        score += 10;

                        Random rand = new Random();
                        int n = rand.nextInt(5);
                        if (n==2){
                            //bonus
                        }

                        //soundPool.play(explodeID, 1, 1, 0, 0, 1);
                        mp2.start();
                        break;
                    } else {
                        if (hitBrickOnSide(bricks[i].getRect(), ball.getRect()) != 0) {
                            hit_point[i] = 1;
                        }
                        if (hitBrickOnBottom(bricks[i].getRect(), ball.getRect()) != 0) {
                            hit_point[i] = 2;
                        }
                    }
                }
            }
        }

        private void addColorToBricks(int i) {
            switch (bricks[i].hits) {
                case 1:
                    paint.setColor(Color.argb(255, 255, 0, 255));
                    break;
                case 2:
                    paint.setColor(Color.argb(255, 0, 0, 255));
                    break;
                case 3:
                    paint.setColor(Color.argb(255, 255, 0, 0));
                    break;
                case 4:
                    paint.setColor(Color.argb(255, 0, 255, 0));
                    break;
                case 5:
                    paint.setColor(Color.argb(255, 0, 255, 255));
                    break;
                case 6:
                    paint.setColor(Color.argb(255, 255, 255, 0));
                    break;
                default:
                    paint.setColor(Color.argb(255, 120, 120, 120));
                    break;
            }
            canvas.drawRect(bricks[i].getRect(), paint);
        }

        // Draw the newly updated scene
        public void draw() {
            if (surfaceHolder.getSurface().isValid()) {
                canvas = surfaceHolder.lockCanvas();

                canvas.drawColor(Color.argb(255, 21, 168, 209));
                canvas.drawBitmap(bitmap, 0, 0, null);
                paint.setColor(Color.argb(255, 255, 255, 255));
                canvas.drawRect(paddle.getRect(), paint);
                canvas.drawOval(ball.getRect(), paint);
                paint.setColor(Color.argb(255, 90, 240, 70));

                for (int i = 0; i < numBricks; i++) {
                    if (bricks[i].getVisibility()) {
                        addColorToBricks(i);
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
                    mp4.start();
                    if (level % 3 == 2)
                        bitmap = BitmapFactory.decodeResource(res, R.drawable.back2);
                    else if (level % 3 == 0)
                        bitmap = BitmapFactory.decodeResource(res, R.drawable.back3);
                    else
                        bitmap = BitmapFactory.decodeResource(res, R.drawable.back1);
                    //if (level == 3) {
                    //    endGame = true;
                    //    canvas.drawText("WYGRANA!", 10, screenY / 2, paint);
                    //} else {
                    createBricks();
                    //}
                }

                // Lost
                else if (lives < 1) {
                    paint.setTextSize(90);
                    paused = true;
                    endGame = true;
                    canvas.drawText("PRZEGRANA!", 10, screenY / 2, paint);
                }
                surfaceHolder.unlockCanvasAndPost(canvas);
            }
        }

        private boolean winLevel() {
            for (int i = 0; i < numBricks; i++) {
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
                        endGame = false;
                        playGame = true;
                        paused = false;
                        if (lives == 0) {
                            resetGame();
                        }
                        else createBricks();
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

        private void afterGame() {
            canvas.drawText("KONIEC GRY! ZAGRAJ JESZCZE RAZ!", 10, screenY / 2, paint);
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