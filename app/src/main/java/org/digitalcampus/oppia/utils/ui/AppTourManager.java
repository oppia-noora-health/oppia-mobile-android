package org.digitalcampus.oppia.utils.ui;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.LifecycleOwner;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.skydoves.balloon.ArrowPositionRules;
import com.skydoves.balloon.Balloon;
import com.skydoves.balloon.BalloonAnimation;
import com.skydoves.balloon.overlay.BalloonOverlayRect;

import org.digitalcampus.mobile.learning.R;

import java.util.ArrayList;
import java.util.List;

public class AppTourManager {

    private final Activity activity;
    private final DrawerLayout drawerLayout;
    private final NavigationView navigationView;
    private final BottomNavigationView bottomNavigationView;
    private final Toolbar toolbar;

    private Balloon currentBalloon;
    private int currentStepIndex = 0;
    private final List<TourStep> steps = new ArrayList<>();

    private static final String PREFS_NAME = "AppTourPrefs";
    private static final String KEY_TOUR_SHOWN = "main_tour_shown";
    private static final String KEY_TOUR_INDEX = "main_tour_step_index";

    private static boolean tourStartedOnce = false;

    public AppTourManager(Activity activity,
                          DrawerLayout drawerLayout,
                          NavigationView navigationView,
                          BottomNavigationView bottomNavigationView,
                          Toolbar toolbar) {
        this.activity = activity;
        this.drawerLayout = drawerLayout;
        this.navigationView = navigationView;
        this.bottomNavigationView = bottomNavigationView;
        this.toolbar = toolbar;
    }

    private boolean isTourShown() {
        SharedPreferences prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_TOUR_SHOWN, false);
    }

    private void markTourShown() {
        SharedPreferences prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_TOUR_SHOWN, true).apply();
    }

    // ------------------------
    // Step Setup
    // ------------------------
    public void prepareSteps() {
        steps.clear();

        steps.add(new TourStep(
                null,
                "Welcome to Noora Academy",
                "Welcome! Here’s a quick guide to help you use the app.",
                null
        ));

        // 2. Download a Course — only if there are no courses
        View noCoursesView = activity.findViewById(R.id.no_courses);
        View emptyStateImg = activity.findViewById(R.id.empty_state_img);

        if (noCoursesView != null && noCoursesView.getVisibility() == View.VISIBLE) {
            if (emptyStateImg != null) {
                addStep(emptyStateImg,
                        "Download a Course",
                        "Tap the ‘+’ sign to download a course from the given category.");
            }
        }

        View hamburger = getHamburger(toolbar);
        addStep(hamburger,
                "Menu Options",
                "Tap the ☰ menu on the top left corner to view your profile, download courses, and explore settings.");

        View pointsView = bottomNavigationView.findViewById(R.id.nav_bottom_points);
        addStep(pointsView,
                "Points",
                "This section shows you the points you have scored, your position on the leaderboard, and your badges/certificates.");
    }

    private void addStep(View target, String title, String desc) {
        steps.add(new TourStep(target, title, desc, null));
    }

    private View getHamburger(Toolbar toolbar) {
        if (toolbar == null) return null;
        for (int i = 0; i < toolbar.getChildCount(); i++) {
            if (toolbar.getChildAt(i) instanceof android.widget.ImageButton)
                return toolbar.getChildAt(i);
        }
        return null;
    }

    // ------------------------
    // Tour Lifecycle
    // ------------------------
    public void startTourIfFirstLaunch() {
        if (isTourShown() || tourStartedOnce) return;
        tourStartedOnce = true;

        SharedPreferences prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        currentStepIndex = prefs.getInt(KEY_TOUR_INDEX, 0);

        activity.getWindow().getDecorView().post(() -> {
            prepareSteps();
            if (!steps.isEmpty()) showStep(steps.get(currentStepIndex));
        });
    }

    private void showStep(TourStep step) {
        if (step == null) return;

        clearAllHighlights();

        if (currentBalloon != null) {
            try {
                currentBalloon.dismiss();
            } catch (Exception ignored) {}
            currentBalloon = null;
        }

        boolean isFirstStep = (currentStepIndex == 0 || step.targetView == null);

        if (isFirstStep) {
            // create and attach a full-screen dim overlay manually (guaranteed full-screen)
            final ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
            final View dimOverlay = new View(activity);
            FrameLayout.LayoutParams overlayParams = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
            );
            dimOverlay.setLayoutParams(overlayParams);
            dimOverlay.setBackgroundColor(activity.getResources().getColor(R.color.overlay_dim)); // ensure this color has alpha
            dimOverlay.setClickable(true); // blocks touches
            // consume all touch events on the overlay
            dimOverlay.setOnTouchListener((v, event) -> true);

            // add overlay to decorView
            decorView.addView(dimOverlay);

            // Build a balloon WITHOUT relying on its internal overlay (we already have our full-screen dim)
            Balloon.Builder builder = new Balloon.Builder(activity)
                    .setLayout(R.layout.layout_tooltip)
                    .setArrowSize(0) // hide arrow for welcome step
                    .setCornerRadius(8f)
                    .setWidthRatio(0.9f)
                    .setBackgroundColorResource(R.color.white)
                    .setElevation(0)
                    .setPadding(12)
                    // disable balloon overlay features to avoid conflicts
                    .setIsVisibleOverlay(false)
                    .setDismissWhenOverlayClicked(false)
                    .setDismissWhenTouchOutside(false)
                    .setBalloonAnimation(BalloonAnimation.FADE)
                    .setLifecycleOwner(activity instanceof LifecycleOwner ? (LifecycleOwner) activity : null);

            currentBalloon = builder.build();

            // ensure balloon overlay touches (if any) are consumed (safety)
            currentBalloon.setOnBalloonOverlayTouchListener((View v, MotionEvent event) -> true);

            View layout = currentBalloon.getContentView();
            TextView title = layout.findViewById(R.id.tvTooltipTitle);
            TextView text = layout.findViewById(R.id.tvTooltipText);
            Button next = layout.findViewById(R.id.btnNext);
            Button skip = layout.findViewById(R.id.btnSkip);

            if (title != null) title.setText(step.title);
            if (text != null) text.setText(step.description);

            // helper to remove overlay safely
            Runnable removeDimOverlay = () -> {
                try {
                    if (decorView.indexOfChild(dimOverlay) != -1) {
                        decorView.removeView(dimOverlay);
                    }
                } catch (Exception ignored) {}
            };

            // Next button
            if (next != null) {
                next.setOnClickListener(v -> {
                    try { currentBalloon.dismiss(); } catch (Exception ignored) {}
                    removeDimOverlay.run();
                    goNext();
                });
            }

            // Skip button
            if (skip != null) {
                skip.setOnClickListener(v -> {
                    try { currentBalloon.dismiss(); } catch (Exception ignored) {}
                    removeDimOverlay.run();
                    endTour();
                });
            }

            // If balloon is dismissed by code elsewhere, ensure we cleanup the overlay
            try {
                currentBalloon.setOnBalloonDismissListener(() -> removeDimOverlay.run());
            } catch (Exception ignored) {
                // Older versions may not have setOnBalloonDismissListener; we already removed in click handlers
            }

            // Show the balloon centered on decorView (guaranteed full-screen dim)
            currentBalloon.showAtCenter(decorView);

            return;
        }


        // ✅ Step 2+: Balloon tooltip with blocked touches

        int[] loc = new int[2];
        step.targetView.getLocationOnScreen(loc);
        float centerX = loc[0] + (step.targetView.getWidth() / 2f);

        int screenWidth = activity.getResources().getDisplayMetrics().widthPixels;

        // Threshold for "middle" area (you can adjust)
        float centerMin = screenWidth * 0.33f;
        float centerMax = screenWidth * 0.66f;

        Float arrowPos = null;

        // If target is left area → arrow goes left
        if (centerX < centerMin) {
            arrowPos = 0.1f;
        }
        // If target is right area → arrow goes right
        else if (centerX > centerMax) {
            arrowPos = 0.9f;
        }
        // If middle → keep default (center arrow)
        // arrowPos stays null

        Balloon.Builder builder = new Balloon.Builder(activity)
                .setLayout(R.layout.layout_tooltip)
                .setArrowSize(12)
                .setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)
                .setCornerRadius(8f)
                .setWidthRatio(0.9f)
                .setBackgroundColorResource(R.color.white)
                .setElevation(0)
                .setPadding(12)
                .setIsVisibleOverlay(true)
                .setOverlayShape(BalloonOverlayRect.INSTANCE)
                .setOverlayPadding(8f)
                .setOverlayColorResource(R.color.overlay_dim)
                .setDismissWhenOverlayClicked(false)
                .setDismissWhenTouchOutside(false)
                .setBalloonAnimation(BalloonAnimation.FADE)
                .setLifecycleOwner(activity instanceof LifecycleOwner ? (LifecycleOwner) activity : null);

        if (arrowPos != null) {
            builder.setArrowPosition(arrowPos);
        }
        currentBalloon = builder.build();

        // ✅ Block touches on overlay completely
        currentBalloon.setOnBalloonOverlayTouchListener((View v, MotionEvent event) -> true);

        View layout = currentBalloon.getContentView();
        TextView title = layout.findViewById(R.id.tvTooltipTitle);
        TextView text = layout.findViewById(R.id.tvTooltipText);
        Button next = layout.findViewById(R.id.btnNext);
        Button skip = layout.findViewById(R.id.btnSkip);

        if (title != null) title.setText(step.title);
        if (text != null) text.setText(step.description);

        next.setOnClickListener(v -> {
            try {
                currentBalloon.dismiss();
            } catch (Exception ignored) {}
            goNext();
        });

        skip.setOnClickListener(v -> endTour());

        try {
            step.targetView.setBackgroundResource(R.drawable.tooltip_highlight);
            currentBalloon.showAlignBottom(step.targetView);
            blockAllTouches(step.targetView);
        } catch (Exception e) {
            Log.d("APP_TOUR", "Balloon alignment failed: " + e.getMessage());
            currentBalloon.showAtCenter(activity.findViewById(android.R.id.content));
        }

        boolean isLastStep = (currentStepIndex == steps.size() - 1);
        if (isLastStep) {
            skip.setVisibility(View.GONE);
            next.setText("Got it");
        } else {
            skip.setVisibility(View.VISIBLE);
            next.setText("Next");
        }
    }

    private void goNext() {
        clearAllHighlights();
        restoreTouch();
        currentStepIndex++;
        activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putInt(KEY_TOUR_INDEX, currentStepIndex)
                .apply();

        if (currentStepIndex < steps.size()) showStep(steps.get(currentStepIndex));
        else endTour();
    }

    private void endTour() {
        markTourShown();
        clearAllHighlights();
        restoreTouch();
        try {
            if (currentBalloon != null) currentBalloon.dismiss();
        } catch (Exception ignored) {}
        try {
            if (drawerLayout != null) drawerLayout.closeDrawer(GravityCompat.START);
        } catch (Exception ignored) {}
        Log.d("APP_TOUR", "Main App Tour finished.");
    }

    private void clearAllHighlights() {
        for (TourStep s : steps) {
            if (s.targetView != null) {
                try {
                    s.targetView.setBackground(null);
                } catch (Exception ignored) {}
            }
        }
    }

    // ✅ Block all touches except tooltip buttons
    private void blockAllTouches(View targetView) {
        View rootView = activity.findViewById(android.R.id.content);
        if (rootView != null)
            rootView.setOnTouchListener((v, e) -> true);

        if (targetView != null)
            targetView.setOnTouchListener((v, e) -> true);
    }

    // ✅ Restore all touch interactions
    private void restoreTouch() {
        View rootView = activity.findViewById(android.R.id.content);
        if (rootView != null)
            rootView.setOnTouchListener(null);

        for (TourStep s : steps) {
            if (s.targetView != null)
                s.targetView.setOnTouchListener(null);
        }
    }

    public static class TourStep {
        final View targetView;
        final String title;
        final String description;
        final Runnable onComplete;

        TourStep(View v, String t, String d, Runnable onDone) {
            this.targetView = v;
            this.title = t;
            this.description = d;
            this.onComplete = onDone;
        }
    }
}
