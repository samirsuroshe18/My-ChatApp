package com.example.mychatapp;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.AnimatorSet;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {
    private static final int SPLASH_DELAY = 3000;
    private static final int ICON_ANIMATION_DURATION = 800;
    private static final int APP_NAME_ANIMATION_DURATION = 300;
    private static final int APP_NAME_ANIMATION_DELAY = 100;
    private static final int TAGLINE_ANIMATION_DURATION = 300;
    private static final int TAGLINE_ANIMATION_DELAY = 200;
    private static final int FEATURES_ANIMATION_DURATION = 400;
    private static final int FEATURES_ANIMATION_DELAY = 300;
    private static final int LOADING_INDICATOR_DELAY = 1000;
    private static final int LOADING_INDICATOR_DURATION = 300;
    private static final int VERSION_TEXT_DELAY = 1500;
    private static final int VERSION_TEXT_DURATION = 400;
    private static final int FADE_OUT_DURATION = 300;
    private static final float OVERSHOOT_TENSION = 1.2f;
    private static final float VERSION_TEXT_ALPHA = 0.7f;
    private static final float SLIDE_UP_DISTANCE = 50f;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference mDatabase;
    private MaterialCardView iconContainer;
    private View appName, appTagline, featuresContainer;
    private ConstraintLayout contentContainer;
    private CircularProgressIndicator loadingIndicator;
    private View versionText;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);

        setupWindowInsets();
        initViews();
        startAnimations();
        navigateToMainActivity();
    }

    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initViews() {
        // Initialize Firebase components
        firebaseAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialize UI components
        iconContainer = findViewById(R.id.icon_container);
        appName = findViewById(R.id.app_name);
        appTagline = findViewById(R.id.app_tagline);
        featuresContainer = findViewById(R.id.features_container);
        contentContainer = findViewById(R.id.content_container);
        loadingIndicator = findViewById(R.id.loading_indicator);
        versionText = findViewById(R.id.version_text);

        // Set initial visibility for animation
        contentContainer.setAlpha(0f);
        loadingIndicator.setAlpha(0f);
        versionText.setAlpha(0f);
    }

    private void startAnimations() {
        animateMainContent();
        animateLoadingIndicator();
        animateVersionText();
    }

    private void animateMainContent() {
        // Create icon animation
        ObjectAnimator iconScaleX = ObjectAnimator.ofFloat(iconContainer, "scaleX", 0.5f, 1f);
        ObjectAnimator iconScaleY = ObjectAnimator.ofFloat(iconContainer, "scaleY", 0.5f, 1f);
        ObjectAnimator iconAlpha = ObjectAnimator.ofFloat(contentContainer, "alpha", 0f, 1f);

        AnimatorSet iconAnimator = new AnimatorSet();
        iconAnimator.playTogether(iconScaleX, iconScaleY, iconAlpha);
        iconAnimator.setDuration(ICON_ANIMATION_DURATION);
        iconAnimator.setInterpolator(new OvershootInterpolator(OVERSHOOT_TENSION));

        iconAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // Animate subsequent elements in sequence
                animateView(appName, APP_NAME_ANIMATION_DURATION, APP_NAME_ANIMATION_DELAY);
                animateView(appTagline, TAGLINE_ANIMATION_DURATION, TAGLINE_ANIMATION_DELAY);
                animateView(featuresContainer, FEATURES_ANIMATION_DURATION, FEATURES_ANIMATION_DELAY);
            }
        });

        iconAnimator.start();
    }

    private void animateView(View view, int duration, int delay) {
        ObjectAnimator slideUp = ObjectAnimator.ofFloat(view, "translationY", SLIDE_UP_DISTANCE, 0f);
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(slideUp, fadeIn);
        animatorSet.setDuration(duration);
        animatorSet.setStartDelay(delay);
        animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
        animatorSet.start();
    }

    private void animateLoadingIndicator() {
        mainHandler.postDelayed(() -> {
            ObjectAnimator fadeIn = ObjectAnimator.ofFloat(loadingIndicator, "alpha", 0f, 1f);
            fadeIn.setDuration(LOADING_INDICATOR_DURATION);
            fadeIn.start();
        }, LOADING_INDICATOR_DELAY);
    }

    private void animateVersionText() {
        mainHandler.postDelayed(() -> {
            ObjectAnimator fadeIn = ObjectAnimator.ofFloat(versionText, "alpha", 0f, VERSION_TEXT_ALPHA);
            fadeIn.setDuration(VERSION_TEXT_DURATION);
            fadeIn.start();
        }, VERSION_TEXT_DELAY);
    }

    private void navigateToMainActivity() {
        mainHandler.postDelayed(() -> {
            // Fade out animation before navigation
            ObjectAnimator fadeOut = ObjectAnimator.ofFloat(contentContainer, "alpha", 1f, 0f);
            fadeOut.setDuration(FADE_OUT_DURATION);
            fadeOut.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    startMainActivity();
                }
            });
            fadeOut.start();
        }, SPLASH_DELAY);
    }

    private void startMainActivity() {
        Bundle extras = getIntent().getExtras();

        if (isNotificationLaunch(extras)) {
            handleNotificationLaunch(extras);
        } else {
            handleNormalLaunch();
        }

        // Add smooth transition
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private boolean isNotificationLaunch(Bundle extras) {
        return extras != null && extras.containsKey("userId");
    }

    private void handleNotificationLaunch(Bundle extras) {
        String userId = extras.getString("userId");
        String userName = extras.getString("userName");
        String profilePic = extras.getString("profilePic");

        // Start home activity first
        Intent mainIntent = new Intent(SplashActivity.this, HomeActivity.class);
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(mainIntent);

        // Start chat detail activity
        Intent chatDetailIntent = new Intent(SplashActivity.this, ChatDetailActivity.class);
        chatDetailIntent.putExtra("userId", userId);
        chatDetailIntent.putExtra("profilePic", profilePic);
        chatDetailIntent.putExtra("userName", userName);
        chatDetailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(chatDetailIntent);

        finish();
    }

    private void handleNormalLaunch() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        Intent intent;

        if (currentUser != null) {
            updateFCMToken();
            intent = new Intent(this, HomeActivity.class);
        } else {
            intent = new Intent(this, SignInActivity.class);
        }

        startActivity(intent);
        finish();
    }

    private void updateFCMToken() {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId != null) {
            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    mDatabase.child("Users").child(userId).child("FCMToken").setValue(task.getResult());
                }
            });
        }
    }
}