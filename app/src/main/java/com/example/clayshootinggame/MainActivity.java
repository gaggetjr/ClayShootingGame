package com.example.clayshootinggame;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    ImageView iv_gun;
    ImageView iv_bullet;
    ImageView iv_clay;
    TextView tvScore;
    int score = 0;
    MediaPlayer gunSound;
    MediaPlayer objectBreakSound;
    boolean canShoot = true;

    int screen_width;
    int screen_height;

    float bullet_width;
    float bullet_height;

    float gun_width;
    float gun_height;

    float clay_width;
    float clay_height;

    float bullet_center_x;
    float bullet_center_y;

    float clay_center_x;
    float clay_center_y;

    float gun_x;
    float gun_y;
    float gun_center_x;

    final int NO_OF_CLAYS = 5;

    ObjectAnimator clay_translateX;
    ObjectAnimator clay_rotation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gunSound = MediaPlayer.create(this, R.raw.gunshot_sound);
        objectBreakSound = MediaPlayer.create(this, R.raw.object_break_sound);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        Button btnStart = findViewById(R.id.btnStart);
        Button btnStop = findViewById(R.id.btnStop);
        tvScore = findViewById(R.id.tvScore);

        tvScore.setText("점수 : 0");

        btnStart.setOnClickListener(this);
        btnStop.setOnClickListener(this);

        ConstraintLayout layout = findViewById(R.id.layout);

        screen_width = Resources.getSystem().getDisplayMetrics().widthPixels;
        screen_height = Resources.getSystem().getDisplayMetrics().heightPixels;

        iv_gun = new ImageView(this);
        iv_bullet = new ImageView(this);
        iv_clay = new ImageView(this);

        iv_gun.setImageResource(R.drawable.gun);
        iv_bullet.setImageResource(R.drawable.bullet);
        iv_clay.setImageResource(R.drawable.clay);

        iv_gun.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        gun_width = iv_gun.getMeasuredWidth();
        gun_height = iv_gun.getMeasuredHeight();

        layout.addView(iv_gun);

        iv_bullet.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        bullet_width = iv_bullet.getMeasuredWidth();
        bullet_height = iv_bullet.getMeasuredHeight();

        iv_bullet.setVisibility(View.INVISIBLE);
        layout.addView(iv_bullet);

        iv_clay.setScaleX(0.8f);
        iv_clay.setScaleY(0.8f);

        iv_clay.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        clay_width = iv_clay.getMeasuredWidth();
        clay_height = iv_clay.getMeasuredHeight();

        layout.addView(iv_clay);

        gun_center_x = screen_width * 0.7f;
        gun_x = gun_center_x - gun_width * 0.5f;
        gun_y = screen_height - gun_height;

        iv_gun.setX(gun_x);
        iv_gun.setY(gun_y);

        iv_gun.setClickable(true);
        iv_gun.setOnClickListener(this);

        iv_clay.setX(0);
        iv_clay.setY(screen_height * 0.1f);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btnStart) {
            gameStart();
        } else if (view.getId() == R.id.btnStop) {
            gameStop();
        } else if (view == iv_gun) {
            shootingStart();
        }
    }

    public void gameStop() {
        finish();
    }

    public void gameStart() {
        score = 0;
        tvScore.setText("점수 : " + score);

        iv_clay.setVisibility(View.VISIBLE);

        clay_translateX = ObjectAnimator.ofFloat(
                iv_clay,
                "translationX",
                -100f,
                (float) screen_width
        );

        clay_rotation = ObjectAnimator.ofFloat(
                iv_clay,
                "rotation",
                0f,
                360f * 5
        );

        clay_translateX.setRepeatCount(NO_OF_CLAYS - 1);
        clay_rotation.setRepeatCount(NO_OF_CLAYS - 1);

        clay_translateX.setDuration(3000);
        clay_rotation.setDuration(3000);

        clay_translateX.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                iv_clay.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                Toast.makeText(getApplicationContext(), "게임 종료", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {
                iv_clay.setVisibility(View.VISIBLE);
            }
        });

        clay_translateX.start();
        clay_rotation.start();
    }

    public void shootingStart() {
        if (!canShoot) {
            return;
        }

        canShoot = false;

        if (gunSound != null) {
            gunSound.seekTo(0);
            gunSound.start();
        }

        float bullet_x = gun_center_x - bullet_width / 2;

        iv_bullet.setX(bullet_x);
        iv_bullet.setY(0);
        iv_bullet.setTranslationY(gun_y);
        iv_bullet.setScaleX(1f);
        iv_bullet.setScaleY(1f);
        iv_bullet.setVisibility(View.VISIBLE);

        ObjectAnimator bullet_scaleDownX = ObjectAnimator.ofFloat(
                iv_bullet,
                "scaleX",
                1f,
                0f
        );

        ObjectAnimator bullet_scaleDownY = ObjectAnimator.ofFloat(
                iv_bullet,
                "scaleY",
                1f,
                0f
        );

        ObjectAnimator bullet_translateY = ObjectAnimator.ofFloat(
                iv_bullet,
                "translationY",
                gun_y,
                0f
        );

        bullet_translateY.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                bullet_center_x = iv_bullet.getX() + bullet_width * 0.5f;
                bullet_center_y = iv_bullet.getY() + bullet_height * 0.5f;

                clay_center_x = iv_clay.getX() + clay_width * 0.5f;
                clay_center_y = iv_clay.getY() + clay_height * 0.5f;

                double dist = Math.sqrt(
                        Math.pow(bullet_center_x - clay_center_x, 2)
                                + Math.pow(bullet_center_y - clay_center_y, 2)
                );

                if (dist <= 100 && iv_clay.getVisibility() == View.VISIBLE) {
                    score += 100;
                    tvScore.setText("점수 : " + score);

                    if (objectBreakSound != null) {
                        objectBreakSound.seekTo(0);
                        objectBreakSound.start();
                    }

                    iv_clay.setVisibility(View.INVISIBLE);
                }
            }
        });

        AnimatorSet bullet = new AnimatorSet();
        bullet.playTogether(
                bullet_translateY,
                bullet_scaleDownX,
                bullet_scaleDownY
        );

        bullet.setDuration(1000);

        bullet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                iv_bullet.setVisibility(View.INVISIBLE);
                canShoot = true;
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });

        bullet.start();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (gunSound != null) {
            gunSound.release();
            gunSound = null;
        }

        if (objectBreakSound != null) {
            objectBreakSound.release();
            objectBreakSound = null;
        }
    }
}