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
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    private DatabaseReference mDatabase;
    private static final int SPLASH_DELAY = 3000;
    private MaterialCardView iconContainer;
    private View appName, appTagline, featuresContainer;
    private ConstraintLayout contentContainer;
    private CircularProgressIndicator loadingIndicator;
    private View versionText;
    final String[] token = new String[1];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize views
        initViews();

        // Start animations
        startAnimations();

        // Navigate to main activity after delay
        navigateToMainActivity();
    }

    private void initViews() {
        firebaseAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
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

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if(task.isSuccessful()){
                    token[0] = task.getResult();
                }
            }
        });
    }

    private void startAnimations() {
        // Animate main content with staggered effect
        animateMainContent();

        // Animate loading indicator
        animateLoadingIndicator();

        // Animate version text
        animateVersionText();
    }

    private void animateMainContent() {
        // Scale and fade in the icon container
        ObjectAnimator iconScaleX = ObjectAnimator.ofFloat(iconContainer, "scaleX", 0.5f, 1f);
        ObjectAnimator iconScaleY = ObjectAnimator.ofFloat(iconContainer, "scaleY", 0.5f, 1f);
        ObjectAnimator iconAlpha = ObjectAnimator.ofFloat(contentContainer, "alpha", 0f, 1f);

        AnimatorSet iconAnimator = new AnimatorSet();
        iconAnimator.playTogether(iconScaleX, iconScaleY, iconAlpha);
        iconAnimator.setDuration(800);
        iconAnimator.setInterpolator(new OvershootInterpolator(1.2f));

        iconAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // Animate app name
                animateView(appName, 300, 100);

                // Animate tagline
                animateView(appTagline, 300, 200);

                // Animate features
                animateView(featuresContainer, 400, 300);
            }
        });

        iconAnimator.start();
    }

    private void animateView(View view, int duration, int delay) {
        ObjectAnimator slideUp = ObjectAnimator.ofFloat(view, "translationY", 50f, 0f);
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(slideUp, fadeIn);
        animatorSet.setDuration(duration);
        animatorSet.setStartDelay(delay);
        animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
        animatorSet.start();
    }

    private void animateLoadingIndicator() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            ObjectAnimator fadeIn = ObjectAnimator.ofFloat(loadingIndicator, "alpha", 0f, 1f);
            fadeIn.setDuration(300);
            fadeIn.start();
        }, 1000);
    }

    private void animateVersionText() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            ObjectAnimator fadeIn = ObjectAnimator.ofFloat(versionText, "alpha", 0f, 0.7f);
            fadeIn.setDuration(400);
            fadeIn.start();
        }, 1500);
    }

    private void navigateToMainActivity() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // Fade out animation before navigation
            ObjectAnimator fadeOut = ObjectAnimator.ofFloat(contentContainer, "alpha", 1f, 0f);
            fadeOut.setDuration(300);
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
        if(extras != null && extras.containsKey("userId")){
            //from notification
            String userId = extras.getString("userId");
            String userName = extras.getString("userName");
            String profilePic = extras.getString("profilePic");

            Intent mainIntent = new Intent(SplashActivity.this, HomeActivity.class);
            mainIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(mainIntent);

            Intent chatDetailIntent = new Intent(SplashActivity.this, ChatDetailActivity.class);
            chatDetailIntent.putExtra("userId", userId);
            chatDetailIntent.putExtra("profilePic", profilePic);
            chatDetailIntent.putExtra("userName", userName);
            chatDetailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(chatDetailIntent);

            finish();

        }else{
            FirebaseUser currentUser = firebaseAuth.getCurrentUser();
            Intent intent;
            if (currentUser != null) {
                String userId = FirebaseAuth.getInstance().getUid();
                FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if(task.isSuccessful()){
                            mDatabase.child("Users").child(userId).child("FCMToken").setValue(task.getResult());
                        }
                    }
                });
                intent = new Intent(this, HomeActivity.class);
            }else {
                intent = new Intent(this, SignInActivity.class);
            }
            startActivity(intent);
            finish();
        }

        // Add smooth transition
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    public void onBackPressed() {
        // Prevent back press during splash screen
        super.onBackPressed();
    }
}