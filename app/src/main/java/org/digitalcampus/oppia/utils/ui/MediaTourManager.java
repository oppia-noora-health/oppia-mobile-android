package org.digitalcampus.oppia.utils.ui;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.lifecycle.LifecycleOwner;

import com.skydoves.balloon.Balloon;
import com.skydoves.balloon.BalloonAnimation;
import com.skydoves.balloon.overlay.BalloonOverlayRect;

import org.digitalcampus.mobile.learning.R;

public class MediaTourManager {

    private static final String PREFS_NAME = "MediaTourPrefs";
    private static final String KEY_DOWNLOAD_TOUR_SHOWN = "download_tour_shown";
    private static final String KEY_SELECTALL_TOUR_SHOWN = "selectall_tour_shown";

    private final Activity activity;

    public MediaTourManager(Activity activity) {
        this.activity = activity;
    }

    private boolean isDownloadTourShown() {
        SharedPreferences prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_DOWNLOAD_TOUR_SHOWN, false);
    }

    private boolean isSelectAllTourShown() {
        SharedPreferences prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_SELECTALL_TOUR_SHOWN, false);
    }

    private void markDownloadTourShown() {
        SharedPreferences prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_DOWNLOAD_TOUR_SHOWN, true).apply();
    }

    private void markSelectAllTourShown() {
        SharedPreferences prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_SELECTALL_TOUR_SHOWN, true).apply();
    }

    /**
     * Step 1: Highlight the download button
     */
    public void startDownloadButtonTour(View downloadButton) {
        if (isDownloadTourShown() || downloadButton == null) return;

        downloadButton.post(() -> {

            // ------------------------------
            // Dynamic Arrow Position
            // ------------------------------
            Float arrowPos = null;

            int[] loc = new int[2];
            downloadButton.getLocationOnScreen(loc);
            float centerX = loc[0] + (downloadButton.getWidth() / 2f);

            int screenWidth = activity.getResources().getDisplayMetrics().widthPixels;

            float centerMin = screenWidth * 0.33f;
            float centerMax = screenWidth * 0.66f;

            if (centerX < centerMin) {
                arrowPos = 0.1f; // left
            } else if (centerX > centerMax) {
                arrowPos = 0.9f; // right
            }
            // else → center → arrowPos = null so default is used

            // ------------------------------
            // Build Balloon Normally
            // ------------------------------
            Balloon.Builder builder = new Balloon.Builder(activity)
                    .setLayout(R.layout.layout_tooltip)
                    .setArrowSize(12)
                    .setCornerRadius(8f)
                    .setBackgroundColorResource(R.color.white)
                    .setWidthRatio(0.9f)
                    .setElevation(0)
                    .setPadding(16)
                    .setBalloonAnimation(BalloonAnimation.FADE)
                    .setIsVisibleOverlay(true)
                    .setOverlayShape(BalloonOverlayRect.INSTANCE)
                    .setOverlayPadding(8f)
                    .setOverlayColorResource(R.color.overlay_dim)
                    .setDismissWhenOverlayClicked(false)
                    .setDismissWhenTouchOutside(false)
                    .setLifecycleOwner(activity instanceof LifecycleOwner ? (LifecycleOwner) activity : null);

            // ✔ Apply arrow only if not center
            if (arrowPos != null)
                builder.setArrowPosition(arrowPos);
            else
                builder.setArrowPosition(0.5f);

            Balloon balloon = builder.build();

            // UI Setup (UNCHANGED)
            View layout = balloon.getContentView();
            TextView title = layout.findViewById(R.id.tvTooltipTitle);
            TextView text = layout.findViewById(R.id.tvTooltipText);
            Button gotIt = layout.findViewById(R.id.btnNext);
            Button skip = layout.findViewById(R.id.btnSkip);

            if (title != null) title.setText("Download Missing Media");
            if (text != null)
                text.setText("Tap this button to download media files needed for the course.");
            if (gotIt != null) {
                gotIt.setText("Got it");
                gotIt.setOnClickListener(v -> {
                    balloon.dismiss();
                    markDownloadTourShown();
                    restoreTouch(downloadButton);
                });
            }
            if (skip != null) skip.setVisibility(View.GONE);

            balloon.showAlignBottom(downloadButton);
            blockAllTouches(downloadButton);
        });
    }

    /**
     * Step 2: Highlight the overflow menu / guide for "Select All"
     */
    public void startSelectAllTour(View anchor) {
        if (anchor == null || isSelectAllTourShown()) return;

        anchor.post(() -> {

            // ------------------------------
            // Dynamic Arrow Position
            // ------------------------------
            Float arrowPos = null;

            int[] loc = new int[2];
            anchor.getLocationOnScreen(loc);
            float centerX = loc[0] + (anchor.getWidth() / 2f);

            int screenWidth = activity.getResources().getDisplayMetrics().widthPixels;

            float centerMin = screenWidth * 0.33f;
            float centerMax = screenWidth * 0.66f;

            if (centerX < centerMin) {
                arrowPos = 0.1f; // left
            } else if (centerX > centerMax) {
                arrowPos = 0.9f; // right
            }

            Balloon.Builder builder = new Balloon.Builder(activity)
                    .setLayout(R.layout.layout_tooltip)
                    .setArrowSize(12)
                    .setCornerRadius(8f)
                    .setBackgroundColorResource(R.color.white)
                    .setWidthRatio(0.8f)
                    .setElevation(0)
                    .setPadding(16)
                    .setBalloonAnimation(BalloonAnimation.FADE)
                    .setIsVisibleOverlay(true)
                    .setOverlayShape(BalloonOverlayRect.INSTANCE)
                    .setOverlayPadding(8f)
                    .setOverlayColorResource(R.color.overlay_dim)
                    .setDismissWhenOverlayClicked(false)
                    .setDismissWhenTouchOutside(false)
                    .setLifecycleOwner(activity instanceof LifecycleOwner ? (LifecycleOwner) activity : null);

            if (arrowPos != null)
                builder.setArrowPosition(arrowPos);
            else
                builder.setArrowPosition(0.5f);

            Balloon balloon = builder.build();

            // UI logic unchanged
            View layout = balloon.getContentView();
            TextView title = layout.findViewById(R.id.tvTooltipTitle);
            TextView text = layout.findViewById(R.id.tvTooltipText);
            Button gotIt = layout.findViewById(R.id.btnNext);
            Button skip = layout.findViewById(R.id.btnSkip);

            if (title != null) title.setText("Download All Media");
            if (text != null)
                text.setText("Click the three dots and choose “Select All” to download all media at once. You can also download individual files using their download icons.");
            if (gotIt != null) {
                gotIt.setText("Got it");
                gotIt.setOnClickListener(v -> {
                    balloon.dismiss();
                    markSelectAllTourShown();
                    restoreTouch(anchor);
                });
            }
            if (skip != null) skip.setVisibility(View.GONE);

            balloon.showAlignBottom(anchor);
            blockAllTouches(anchor);
        });
    }

    private void restoreTouch(View targetView) {
        View rootView = activity.findViewById(android.R.id.content);
        if (rootView != null) rootView.setOnTouchListener(null);
        if (targetView != null) targetView.setOnTouchListener(null);
    }

    private void blockAllTouches(View targetView) {
        // Block all touches on the underlying UI
        View rootView = activity.findViewById(android.R.id.content);
        if (rootView != null) {
            rootView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return true; // block touches everywhere
                }
            });
        }

        // Block touches on the target (download/select all button)
        if (targetView != null) {
            targetView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return true; // block clicks on the highlighted view
                }
            });
        }
    }
}
