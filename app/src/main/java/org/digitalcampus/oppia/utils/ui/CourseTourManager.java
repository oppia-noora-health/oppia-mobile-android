package org.digitalcampus.oppia.utils.ui;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.viewpager.widget.ViewPager;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;
import com.skydoves.balloon.Balloon;
import com.skydoves.balloon.BalloonAnimation;

import org.digitalcampus.mobile.learning.R;

public class CourseTourManager {

    private static final String PREFS_NAME = "CourseTourPrefs";
    private static final String KEY_TOUR_SHOWN = "course_tour_shown";

    private final Activity activity;

    public CourseTourManager(Activity activity) {
        this.activity = activity;
    }

    private boolean isTourShown() {
        SharedPreferences prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_TOUR_SHOWN, false);
    }

    private void markTourShown() {
        SharedPreferences prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_TOUR_SHOWN, true).apply();
    }

//    public void startCourseTourIfFirstLaunch(View swipeTarget) {
//        if (isTourShown()) return;
//
//        ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
//
//        // 🔹 Circle animation view (slightly above tooltip)
//        LottieAnimationView circleAnim = new LottieAnimationView(activity);
//        FrameLayout.LayoutParams circleParams = new FrameLayout.LayoutParams(
//                200, 200 // increased size for visibility
//        );
//        circleParams.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
//        circleParams.bottomMargin = 800; // just above tooltip
//        circleAnim.setLayoutParams(circleParams);
//        circleAnim.setAnimation(R.raw.circle_swipe);
//        circleAnim.setRepeatCount(LottieDrawable.INFINITE);
//        circleAnim.setSpeed(0.5f);
//        circleAnim.playAnimation();
//
//        // Make sure it's above tooltip
//        circleAnim.setZ(100f); // bring to front
//        circleAnim.bringToFront();
//
//        decorView.addView(circleAnim);
//
//        // 🔹 Tooltip layout
//        View tooltipView = activity.getLayoutInflater().inflate(R.layout.layout_swipe_tour, decorView, false);
//        FrameLayout.LayoutParams tooltipParams = new FrameLayout.LayoutParams(
//                FrameLayout.LayoutParams.WRAP_CONTENT,
//                FrameLayout.LayoutParams.WRAP_CONTENT
//        );
//        tooltipParams.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
//        tooltipParams.bottomMargin = 200;
//        tooltipView.setLayoutParams(tooltipParams);
//        decorView.addView(tooltipView);
//
//        TextView text = tooltipView.findViewById(R.id.tvSwipeText);
//        Button gotIt = tooltipView.findViewById(R.id.btnGotIt);
//        if (text != null)
//            text.setText("Swipe left or right to move between screens in a lesson.");
//
//        gotIt.setOnClickListener(v -> {
//            removeTourViews(decorView, circleAnim, tooltipView);
//            markTourShown();
//        });
//
//        // 🔹 End tour if user swipes manually
//        swipeTarget.setOnTouchListener((v, event) -> {
//            if (event.getAction() == MotionEvent.ACTION_MOVE) {
//                removeTourViews(decorView, circleAnim, tooltipView);
//                markTourShown();
//                swipeTarget.setOnTouchListener(null);
//            }
//            return false;
//        });
//
//        // 🔹 Auto-swipe once with diagonal drift and easing
//        if (swipeTarget instanceof ViewPager) {
//            ViewPager pager = (ViewPager) swipeTarget;
//            final int totalPages = pager.getAdapter() != null ? pager.getAdapter().getCount() : 0;
//
//            if (totalPages > 1) {
//                final Handler handler = new Handler();
//                int current = pager.getCurrentItem();
//                int nextIndex = Math.min(current + 1, totalPages - 1);
//
//                // Step 1: Appear on first tab (already visible)
//                circleAnim.setAlpha(1f);
//
//                // Step 2: After short delay, move diagonally right-down and fade out
//                handler.postDelayed(() -> {
//                    circleAnim.animate()
//                            .translationXBy(120f)
//                            .translationYBy(40f)
//                            .alpha(0f)
//                            .setDuration(1000)
//                            .setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator())
//                            .withEndAction(() -> {
//                                // Step 3: Perform the actual page swipe
//                                pager.setCurrentItem(nextIndex, true);
//                            })
//                            .start();
//                }, 1800);
//
//                // Step 4: Wait for swipe animation, then fade back in from opposite side
//                handler.postDelayed(() -> {
//                    circleAnim.setTranslationX(-120f);
//                    circleAnim.setTranslationY(-40f);
//                    circleAnim.animate()
//                            .translationX(0f)
//                            .translationY(0f)
//                            .alpha(1f)
//                            .setDuration(900)
//                            .setInterpolator(new android.view.animation.DecelerateInterpolator())
//                            .start();
//                }, 3400);
//
//                // Step 5: Hold briefly, then dismiss all
//                handler.postDelayed(() -> {
//                    removeTourViews(decorView, circleAnim, tooltipView);
//                    markTourShown();
//                }, 6000);
//            }
//        }
//
//        // 🔹 Fallback auto-dismiss
//        new Handler().postDelayed(() -> {
//            removeTourViews(decorView, circleAnim, tooltipView);
//            markTourShown();
//        }, 12000);
//    }

    public void startCourseTourIfFirstLaunch(View swipeTarget) {
        if (isTourShown()) return;

        ViewGroup decor = (ViewGroup) activity.getWindow().getDecorView();

        // ----------------------------------------
        // 1) Add circle Lottie Animation
        // ----------------------------------------
        LottieAnimationView circle = new LottieAnimationView(activity);
        FrameLayout.LayoutParams circleParams = new FrameLayout.LayoutParams(
                200, 200
        );
        circleParams.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
        circleParams.bottomMargin = 600;
        circle.setLayoutParams(circleParams);

        circle.setAnimation(R.raw.circle_swipe);
        circle.setRepeatCount(LottieDrawable.INFINITE);
        circle.setSpeed(0.5f);
        circle.playAnimation();
        circle.setZ(50f);

        decor.addView(circle);

        // ----------------------------------------
        // 2) Create Balloon Tooltip (matching first step design)
        // ----------------------------------------
        Balloon balloon = new Balloon.Builder(activity)
                .setLayout(R.layout.layout_swipe_tour)     // your matching design
                .setArrowSize(0) // hide arrow for welcome step
                .setCornerRadius(8f)
                .setWidthRatio(0.9f)
                .setBackgroundColorResource(R.color.white)
                .setElevation(0)
                .setPadding(12)
                .setBalloonAnimation(BalloonAnimation.FADE)
                .setDismissWhenTouchOutside(false)
                .setIsVisibleOverlay(false)                // no dim
                .build();

        // Set text + button behavior
        View layout = balloon.getContentView();
        TextView title = layout.findViewById(R.id.tvTooltipTitle);
        TextView text = layout.findViewById(R.id.tvSwipeText);
        Button gotIt = layout.findViewById(R.id.btnGotIt);

        if (text != null)
            text.setText("Swipe left or right to move between screens in a lesson.");

        gotIt.setOnClickListener(v -> {
            balloon.dismiss();
            decor.removeView(circle);
            markTourShown();
        });

        // Show bottom center of screen
        View root = activity.findViewById(android.R.id.content);
        balloon.showAlignBottom(root);

        // ----------------------------------------
        // 3) Handle swipe end instantly
        // ----------------------------------------
        swipeTarget.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_MOVE) {
                balloon.dismiss();
                decor.removeView(circle);
                markTourShown();
                swipeTarget.setOnTouchListener(null);
            }
            return false;
        });

        // ----------------------------------------
        // 4) Auto SWIPE animation (same as your version)
        // ----------------------------------------
        if (swipeTarget instanceof ViewPager) {
            ViewPager pager = (ViewPager) swipeTarget;

            int total = pager.getAdapter() != null ? pager.getAdapter().getCount() : 0;
            if (total > 1) {

                Handler handler = new Handler();
                int current = pager.getCurrentItem();
                int next = Math.min(current + 1, total - 1);

                handler.postDelayed(() -> {
                    circle.animate()
                            .translationXBy(120f)
                            .translationYBy(40f)
                            .alpha(0f)
                            .setDuration(900)
                            .withEndAction(() -> pager.setCurrentItem(next, true))
                            .start();
                }, 1800);

                handler.postDelayed(() -> {
                    circle.setTranslationX(-120f);
                    circle.setTranslationY(-40f);
                    circle.animate()
                            .translationX(0f)
                            .translationY(0f)
                            .alpha(1f)
                            .setDuration(800)
                            .start();
                }, 3300);

                handler.postDelayed(() -> {
                    balloon.dismiss();
                    decor.removeView(circle);
                    markTourShown();
                }, 5500);
            }
        }
    }


    private void removeTourViews(ViewGroup root, View circle, View tooltip) {
        try {
            root.removeView(circle);
            root.removeView(tooltip);
        } catch (Exception ignored) {}
    }
}
