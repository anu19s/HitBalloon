package com.example.anuradha.balloonhit;

import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

//import java.sql.Date;
import java.util.Random;
import java.util.*;

import java.util.Date;

public class MainActivity extends AppCompatActivity
implements  Balloon.BalloonListener {

    private static final int MIN_ANIMATION_DELAY=500;
    private static final int MAX_ANIMATION_DELAY=1500;
    private static final int MIN_ANIMATION_DURATION=1000;
    private static final int MAX_ANIMATION_DURATION=8000;
    private static final int NUMBER_OF_PINS=5;

    private ViewGroup mContentView;
    private int[] mBalloonColors=new int[3];
    private int mNextColor, mScreenWidth, mScreenHeight;

    private int mLevel;
    private int mScore,mPinUsed;
    TextView mScoreDisplay,mLevelDisplay;
    private List<ImageView>mPinImages=new ArrayList();
    private List<Balloon> mBalloons =new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBalloonColors[0] = Color.argb(255, 255, 0, 0);
        mBalloonColors[1] = Color.argb(255, 0, 255, 0);
        mBalloonColors[2] = Color.argb(255, 0, 0, 255);


        getWindow().setBackgroundDrawableResource(R.drawable.modern_background);
        mContentView = (ViewGroup) findViewById(R.id.activity_main);

        setToFullScreen();

        ViewTreeObserver viewTreeObserver = mContentView.getViewTreeObserver();
        if (viewTreeObserver.isAlive()) {
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    mContentView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    mScreenWidth = mContentView.getWidth();
                    mScreenHeight = mContentView.getHeight();
                }
            });
        }


        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setToFullScreen();
            }

        });
        mScoreDisplay = (TextView) findViewById(R.id.score_display);
        mLevelDisplay =(TextView) findViewById(R.id.level_display);
        mPinImages.add((ImageView) findViewById(R.id.pushpin1));
        mPinImages.add((ImageView) findViewById(R.id.pushpin2));
        mPinImages.add((ImageView) findViewById(R.id.pushpin3));
        mPinImages.add((ImageView) findViewById(R.id.pushpin4));
        mPinImages.add((ImageView) findViewById(R.id.pushpin5));

        updateDisplay();
    }

//        mContentView.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                if (event.getAction() == MotionEvent.ACTION_UP) {
//                    Balloon b = new Balloon(MainActivity.this, mBalloonColors[mNextColor], 100);
//                    b.setX(event.getX());
//                    b.setY(mScreenHeight);
//                    mContentView.addView(b);
//                    b.releaseBalloon(mScreenHeight,3000);
//                    if (mNextColor+1 ==mBalloonColors.length) {
//                        mNextColor = 0;
//                    }
//                        else {
//                            mNextColor++;
//
//                    }
//                }
//                return false;
//            }
//        });


   // }
    private void setToFullScreen() {
        ViewGroup rootLayout=(ViewGroup) findViewById(R.id.activity_main);
        rootLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    @Override
    protected  void onResume(){
        super.onResume();
        setToFullScreen();
    }

    private void startLevel() {
        mLevel++;
        updateDisplay();
        BalloonLauncher launcher=new BalloonLauncher();
        launcher.execute(mLevel);

    }

    public void goButtonClickHandler(View view) {
        startLevel();
    }

    @Override
    public void popBalloon(Balloon balloon, boolean userTouch){

        mContentView.removeView(balloon);
        mBalloons.remove(balloon);
        if (userTouch){
            mScore++;
        } else {
            mPinUsed++;
            if (mPinUsed<=mPinImages.size()) {
                mPinImages.get(mPinUsed-1)
                        .setImageResource(R.drawable.pin_off);
            }
            if (mPinUsed== NUMBER_OF_PINS) {
                gameOver(true);
                return;
            }
            else {
                Toast.makeText(this,"Missed that one",Toast.LENGTH_SHORT).show();
            }
        }
        updateDisplay();
    }

    private void gameOver(boolean b){
        Toast.makeText(this,"Game Over", Toast.LENGTH_SHORT).show();
        for (Balloon balloon : mBalloons)
        {
           mContentView.removeView(balloon);
            balloon.setPopped(true);
        }
        mBalloons.clear();
    }

    private void updateDisplay() {
        mScoreDisplay.setText(String.valueOf(mScore));
        mLevelDisplay.setText(String.valueOf(mLevel));
    }


    private class BalloonLauncher extends AsyncTask<Integer, Integer, Void> {

        @Override
        protected Void doInBackground(Integer... params) {

            if (params.length != 1) {
                throw new AssertionError(
                        "Expected 1 param for current level");
            }

            int level = params[0];
            int maxDelay = Math.max(MIN_ANIMATION_DELAY,
                    (MAX_ANIMATION_DELAY - ((level - 1) * 500)));
            int minDelay = maxDelay / 2;

            int balloonsLaunched = 0;
            while (balloonsLaunched < 3) {

//              Get a random horizontal position for the next balloon
                Date date = new Date();
                Random random = new Random(date.getTime());
                int xPosition = random.nextInt(mScreenWidth - 200);
                publishProgress(xPosition);
                balloonsLaunched++;

//              Wait a random number of milliseconds before looping
                int delay = random.nextInt(minDelay) + minDelay;
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            return null;

        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            int xPosition = values[0];
            launchBalloon(xPosition);
        }

    }

    private void launchBalloon(int x) {

        Balloon balloon = new Balloon(this, mBalloonColors[mNextColor], 150);
            mBalloons.add(balloon);
        if (mNextColor + 1 == mBalloonColors.length) {
            mNextColor = 0;
        } else {
            mNextColor++;
        }

//      Set balloon vertical position and dimensions, add to container
        balloon.setX(x);
        balloon.setY(mScreenHeight + balloon.getHeight());
        mContentView.addView(balloon);

//      Let 'er fly
        int duration = Math.max(MIN_ANIMATION_DURATION, MAX_ANIMATION_DURATION - (mLevel * 1000));
        balloon.releaseBalloon(mScreenHeight, duration);

    }
}
