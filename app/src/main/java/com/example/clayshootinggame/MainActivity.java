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

import java.util.Random;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    ImageView iv_gun;
    ImageView iv_bullet;
    ImageView iv_clay;
    TextView tvScore;
    TextView tvResult;
    int score = 0;

    MediaPlayer gunSound;
    MediaPlayer objectBreakSound;

    boolean canShoot = true;
    boolean isGameRunning = false;

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
    AnimatorSet claySet;

    Random random = new Random();
    int clayCount = 0;

    float shootAngle = 0f; // 0도는 위로 직선 발사
    final float MIN_ANGLE = -35f;
    final float MAX_ANGLE = 35f;
    final float ANGLE_STEP = 10f;

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
        Button btnLeft = findViewById(R.id.btnLeft);
        Button btnRight = findViewById(R.id.btnRight);

        tvScore = findViewById(R.id.tvScore);
        tvScore.setText("점수 : 0");

        tvResult = findViewById(R.id.tvResult);
        tvResult.setVisibility(View.GONE);

        btnStart.setOnClickListener(this);
        btnStop.setOnClickListener(this);
        btnLeft.setOnClickListener(this);
        btnRight.setOnClickListener(this);

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

        gun_center_x = screen_width * 0.5f; // 가운데 위치변경
        gun_x = gun_center_x - gun_width * 0.5f;
        gun_y = screen_height - gun_height;

        iv_gun.setX(gun_x);
        iv_gun.setY(gun_y);

        // 총 회전 중심
        iv_gun.setPivotX(gun_width * 0.5f);
        iv_gun.setPivotY(gun_height * 0.5f);

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
        } else if (view.getId() == R.id.btnLeft) {
            changeGunAngle(-ANGLE_STEP);
        } else if (view.getId() == R.id.btnRight) {
            changeGunAngle(ANGLE_STEP);
        } else if (view == iv_gun) {
            shootingStart();
        }
    }

    public void gameStop() {
        isGameRunning = false;

        if (claySet != null) {
            claySet.removeAllListeners();
            claySet.cancel();
            claySet = null;
        }

        finish();
    }

    public void changeGunAngle(float amount) {
        shootAngle += amount;

        if (shootAngle < MIN_ANGLE) {
            shootAngle = MIN_ANGLE;
        }

        if (shootAngle > MAX_ANGLE) {
            shootAngle = MAX_ANGLE;
        }

        iv_gun.setRotation(shootAngle);
    }

    public void gameStart() {
        score = 0;
        tvScore.setText("점수 : " + score);

        tvResult.setText("");
        tvResult.setVisibility(View.GONE);

        clayCount = 0;
        isGameRunning = true;

        if (claySet != null) {
            claySet.removeAllListeners();
            claySet.cancel();
            claySet = null;
        }

        playNextClay();
    }

    public void showResultMessage() {
        String message = "";

        if (score == 0) {
            message = "실수로 게임 시작 버튼을 누르신거죠??!!";
        } else if (score == 100) {
            message = "운이 좋으시네요.";
        } else if (score == 200 || score == 300) {
            message = "좋습니다.";
        } else if (score == 400) {
            message = "사실상 만점";
        } else if (score == 500) {
            message = "조작감이 구리실텐데 어떻게하신거죠?;;";
        }

        tvResult.setText(message);
        tvResult.setVisibility(View.VISIBLE);
    }

    public void playNextClay() {
        if (!isGameRunning) {
            return;
        }

        if (clayCount >= NO_OF_CLAYS) {
            iv_clay.setVisibility(View.INVISIBLE);
            isGameRunning = false;

            showResultMessage();

            return;
        }

        clayCount++;

        iv_clay.setVisibility(View.VISIBLE);
        iv_clay.setRotation(0f);
        iv_clay.setTranslationX(0f);
        iv_clay.setTranslationY(0f);

        // 클레이 높이 랜덤
        float randomY = screen_height * (0.08f + random.nextFloat() * 0.35f);

        // 클레이 방향 랜덤
        boolean leftToRight = random.nextBoolean();

        float startX;
        float endX;

        if (leftToRight) {
            startX = -clay_width;
            endX = screen_width + clay_width;
        } else {
            startX = screen_width + clay_width;
            endX = -clay_width;
        }

        iv_clay.setX(startX);
        iv_clay.setY(randomY);

        // 클레이 속도 랜덤
        int randomDuration = 1800 + random.nextInt(1800);

        // 클레이 회전 랜덤
        int rotateCount = 2 + random.nextInt(5);
        float rotationEnd;

        if (leftToRight) {
            rotationEnd = 360f * rotateCount;
        } else {
            rotationEnd = -360f * rotateCount;
        }

        clay_translateX = ObjectAnimator.ofFloat(
                iv_clay,
                "x",
                startX,
                endX
        );

        clay_rotation = ObjectAnimator.ofFloat(
                iv_clay,
                "rotation",
                0f,
                rotationEnd
        );

        claySet = new AnimatorSet();
        claySet.playTogether(clay_translateX, clay_rotation);
        claySet.setDuration(randomDuration);

        claySet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                iv_clay.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                playNextClay();
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });

        claySet.start();
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

        // 총알 시작 위치: 총 근처에서 시작
        float bulletStartX = gun_center_x - bullet_width / 2;
        float bulletStartY = gun_y;

        iv_bullet.setX(bulletStartX);
        iv_bullet.setY(bulletStartY);
        iv_bullet.setScaleX(1f);
        iv_bullet.setScaleY(1f);
        iv_bullet.setRotation(shootAngle);
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

        // 총 각도에 따라 총알 도착 위치 계산
        double radian = Math.toRadians(shootAngle);
        float distance = screen_height * 1.2f;

        float bulletEndX = bulletStartX + (float) (Math.sin(radian) * distance);
        float bulletEndY = bulletStartY + (float) (-Math.cos(radian) * distance);

        // translationX/Y 말고 x/y 자체를 움직임
        ObjectAnimator bullet_translateX = ObjectAnimator.ofFloat(
                iv_bullet,
                "x",
                bulletStartX,
                bulletEndX
        );

        ObjectAnimator bullet_translateY = ObjectAnimator.ofFloat(
                iv_bullet,
                "y",
                bulletStartY,
                bulletEndY
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
                bullet_translateX,
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
                iv_bullet.setVisibility(View.INVISIBLE);
                canShoot = true;
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

        if (claySet != null) {
            claySet.removeAllListeners();
            claySet.cancel();
            claySet = null;
        }

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