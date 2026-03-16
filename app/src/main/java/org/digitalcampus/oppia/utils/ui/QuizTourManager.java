package org.digitalcampus.oppia.utils.ui;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.TextView;

import androidx.lifecycle.LifecycleOwner;

import com.skydoves.balloon.Balloon;
import com.skydoves.balloon.BalloonAnimation;
import com.skydoves.balloon.overlay.BalloonOverlayRect;

import org.digitalcampus.mobile.learning.R;

import java.util.ArrayList;
import java.util.List;

public class QuizTourManager {

    private static final String PREFS_NAME = "QuizTourPrefs";
    private static final String KEY_ANSWER_TOUR_SHOWN = "quiz_answer_tour_shown";
    private static final String KEY_RESULT_TOUR_SHOWN = "quiz_result_tour_shown";

    private final Activity activity;
    private Balloon currentBalloon;
    private int currentStepIndex = 0;
    private final List<TourStep> steps = new ArrayList<>();

    public QuizTourManager(Activity activity) {
        this.activity = activity;
    }

    // ------------------------------
    // FIRST STEP TOUR: Answer Highlight
    // ------------------------------
    public void startAnswerTourIfFirstLaunch(View answerView) {
        SharedPreferences prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        if (prefs.getBoolean(KEY_ANSWER_TOUR_SHOWN, false) || answerView == null) return;

        steps.clear();
        steps.add(new TourStep(answerView, "Attempt a Question",
                "Tap on the correct answer to attempt the quiz.", null));

        currentStepIndex = 0;

        answerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                answerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                showStepSafe();
            }
        });

        prefs.edit().putBoolean(KEY_ANSWER_TOUR_SHOWN, true).apply();
        Log.d("QUIZ_TOUR", "Answer tour started (first launch only).");
    }

    // ------------------------------
    // RESULT TOUR: After quiz finishes
    // ------------------------------
    public void startResultTourIfFirstLaunch(View resultContainer, View retakeBtn, View continueBtn) {
        SharedPreferences prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        if (prefs.getBoolean(KEY_RESULT_TOUR_SHOWN, false) || resultContainer == null) return;

        steps.clear();
        if (retakeBtn != null)
            steps.add(new TourStep(retakeBtn, "Retake Quiz",
                    "Tap here to retake the quiz if you want to try again.", null));
        if (continueBtn != null)
            steps.add(new TourStep(continueBtn, "Continue Course",
                    "Tap CONTINUE to go back to the course.", null));

        if (steps.isEmpty()) return;

        currentStepIndex = 0;
        showStepSafe();
        prefs.edit().putBoolean(KEY_RESULT_TOUR_SHOWN, true).apply();
        Log.d("QUIZ_TOUR", "Result tour started (first launch only).");
    }

    // ------------------------------
    // Core Tour Logic
    // ------------------------------
    private void showStepSafe() {
        if (currentStepIndex < 0 || currentStepIndex >= steps.size()) {
            endTour();
            return;
        }
        showStep(steps.get(currentStepIndex));
    }

    private void showStep(TourStep step) {
        if (step == null) return;

        if (currentBalloon != null) {
            try { currentBalloon.dismiss(); } catch (Exception ignored) {}
            currentBalloon = null;
        }

        // ------------------------------
        // NEW LOGIC: DYNAMIC ARROW POSITION
        // ------------------------------
        Float arrowPos = null;

        if (step.targetView != null) {
            int[] loc = new int[2];
            step.targetView.getLocationOnScreen(loc);
            float centerX = loc[0] + (step.targetView.getWidth() / 2f);

            int screenWidth = activity.getResources().getDisplayMetrics().widthPixels;

            float centerMin = screenWidth * 0.33f;
            float centerMax = screenWidth * 0.66f;

            if (centerX < centerMin) {
                arrowPos = 0.1f;   // Left side
            } else if (centerX > centerMax) {
                arrowPos = 0.9f;   // Right side
            }
            // else → centered → no arrowPos applied
        }

        // ------------------------------
        // BUILD BALLOON WITH CONDITIONAL ARROW POSITION
        // ------------------------------
        Balloon.Builder builder = new Balloon.Builder(activity)
                .setLayout(R.layout.layout_tooltip)
                .setArrowSize(12)
                .setCornerRadius(8f)
                .setBackgroundColorResource(R.color.white)
                .setWidthRatio(0.9f)
                .setElevation(0)
                .setPadding(12)
                .setIsVisibleOverlay(true)
                .setOverlayShape(BalloonOverlayRect.INSTANCE)
                .setOverlayPadding(8f)
                .setOverlayColorResource(R.color.overlay_dim)
                .setBalloonAnimation(BalloonAnimation.FADE)
                .setDismissWhenOverlayClicked(false)
                .setDismissWhenTouchOutside(false)
                .setLifecycleOwner(activity instanceof LifecycleOwner ? (LifecycleOwner) activity : null);

        // Apply arrow position ONLY when not center
        if (arrowPos != null) {
            builder.setArrowPosition(arrowPos);
        } else {
            builder.setArrowPosition(0.5f); // default centered
        }

        currentBalloon = builder.build();

        // ------------------------------
        // UI Setup
        // ------------------------------
        currentBalloon.setOnBalloonOverlayTouchListener((View v, MotionEvent event) -> true);

        View layout = currentBalloon.getContentView();
        TextView title = layout.findViewById(R.id.tvTooltipTitle);
        TextView text = layout.findViewById(R.id.tvTooltipText);
        Button next = layout.findViewById(R.id.btnNext);
        Button skip = layout.findViewById(R.id.btnSkip);

        if (title != null) title.setText(step.title);
        if (text != null) text.setText(step.description);

        if (next != null) {
            next.setOnClickListener(v -> {
                try { currentBalloon.dismiss(); } catch (Exception ignored) {}
                goNext();
            });
        }

        if (skip != null) {
            skip.setOnClickListener(v -> {
                try { currentBalloon.dismiss(); } catch (Exception ignored) {}
                endTour();
            });
        }

        // ------------------------------
        // SHOW TOOLTIP
        // ------------------------------
        if (step.targetView != null) {
            try {
                step.targetView.setBackgroundResource(R.drawable.tooltip_highlight);
                currentBalloon.showAlignBottom(step.targetView);
                blockAllTouches(step.targetView);
            } catch (Exception e) {
                currentBalloon.showAtCenter(activity.findViewById(android.R.id.content));
            }
        } else {
            currentBalloon.showAtCenter(activity.findViewById(android.R.id.content));
        }

        if (next != null)
            next.setText(currentStepIndex == steps.size() - 1 ? "Got it" : "Next");

        if (skip != null)
            skip.setVisibility(currentStepIndex == steps.size() - 1 ? View.GONE : View.VISIBLE);
    }

    // ------------------------------
    // Touch Control Helpers
    // ------------------------------
    private void blockAllTouches(View targetView) {
        View rootView = activity.findViewById(android.R.id.content);
        if (rootView != null)
            rootView.setOnTouchListener((v, event) -> true); // block all touches

        if (targetView != null)
            targetView.setOnTouchListener((v, event) -> true); // block highlighted button touches
    }

    private void restoreTouch() {
        View rootView = activity.findViewById(android.R.id.content);
        if (rootView != null) rootView.setOnTouchListener(null);

        // Explicitly restore each target
        for (TourStep s : steps) {
            if (s.targetView != null) s.targetView.setOnTouchListener(null);
        }
    }

    // ------------------------------
    // Tour Navigation
    // ------------------------------
    private void goNext() {
        clearAllHighlights();
        restoreTouch();
        currentStepIndex++;
        showStepSafe();
    }

    private void endTour() {
        clearAllHighlights();
        restoreTouch();
        if (currentBalloon != null) {
            try { currentBalloon.dismiss(); } catch (Exception ignored) {}
        }
        Log.d("QUIZ_TOUR", "Quiz Tour finished.");
    }

    private void clearAllHighlights() {
        for (TourStep s : steps) {
            if (s.targetView != null) {
                try { s.targetView.setBackground(null); } catch (Exception ignored) {}
            }
        }
    }

    // ------------------------------
    // Data Class for Tour Steps
    // ------------------------------
    public static class TourStep {
        final View targetView;
        final String title;
        final String description;
        final Runnable onComplete;

        public TourStep(View v, String t, String d, Runnable onDone) {
            this.targetView = v;
            this.title = t;
            this.description = d;
            this.onComplete = onDone;
        }
    }
}
