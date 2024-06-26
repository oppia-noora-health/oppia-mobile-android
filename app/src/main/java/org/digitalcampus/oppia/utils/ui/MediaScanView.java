package org.digitalcampus.oppia.utils.ui;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.mobile.learning.databinding.ViewMediaScanBinding;
import org.digitalcampus.oppia.activity.DownloadMediaActivity;
import org.digitalcampus.oppia.listener.ScanMediaListener;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.Media;
import org.digitalcampus.oppia.task.ScanMediaTask;
import org.digitalcampus.oppia.task.result.EntityListResult;

import java.util.List;

public class MediaScanView extends FrameLayout implements ScanMediaListener {

    private ViewMediaScanBinding binding;
    private SharedPreferences prefs;
    private View viewBelow;
    private int initialViewBelowPadding = 0;
    private boolean updateMediaScan;
    private Course mediaCourseFilter;

    public MediaScanView(@NonNull Context context) {
        super(context);
        init();
    }


    public MediaScanView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MediaScanView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {

        prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        binding = ViewMediaScanBinding.inflate(LayoutInflater.from(getContext()));
        addView(binding.getRoot());

        binding.tvMediaMessage.setText(R.string.info_scan_media_missing);
        binding.btnMediaDownload.setText(R.string.scan_media_download_button);

        setVisibility(GONE);
    }

    public void setViewBelow(View viewBelow) {
        this.viewBelow = viewBelow;
        initialViewBelowPadding = viewBelow.getPaddingTop();
    }

    public void setMediaCourseFilter(Course course) {
        this.mediaCourseFilter = course;
    }

    public void setMessage(String message) {
        binding.tvMediaMessage.setText(message);
    }

    public void setUpdateMediaScan(boolean updateMediaScan) {
        this.updateMediaScan = updateMediaScan;
    }

    //region ScanMedia
    ///Everything related to the ScanMediaTask, including UI management

    public void scanMedia(List<Course> courses) {

        if (Media.shouldScanMedia(prefs)) {
            ScanMediaTask task = new ScanMediaTask(getContext());
            task.setScanMediaListener(this);
            task.execute(courses);
        } else {
            hideView();
        }
    }

    private void showView() {

        TranslateAnimation anim = new TranslateAnimation(0, 0, -200, 0);
        anim.setDuration(900);
        startAnimation(anim);

        binding.getRoot().measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ValueAnimator animator = ValueAnimator.ofInt(initialViewBelowPadding, binding.getRoot().getMeasuredHeight());
        animator.addUpdateListener(valueAnimator -> viewBelow.setPaddingRelative(0, (Integer) valueAnimator.getAnimatedValue(), 0, 0));
        animator.setStartDelay(200);
        animator.setDuration(700);
        animator.start();

        setVisibility(VISIBLE);
    }

    private void hideView() {
        setVisibility(View.GONE);
        viewBelow.setPaddingRelative(0, initialViewBelowPadding, 0, 0);

    }


    /* ScanMediaListener implementation */

    // View is not showing until scanComplete() so the messages have been removed
    public void scanStart() {
    }

    public void scanProgressUpdate(String msg) {
    }

    public void scanComplete(EntityListResult<Media> result) {

        if (result.hasItems()) {

            binding.btnMediaDownload.setOnClickListener(view -> {

                Intent i = new Intent(getContext(), DownloadMediaActivity.class);

                if (mediaCourseFilter != null) {
                    Bundle tb = new Bundle();
                    tb.putSerializable(DownloadMediaActivity.MISSING_MEDIA_COURSE_FILTER, mediaCourseFilter);
                    i.putExtras(tb);
                }

                getContext().startActivity(i);
            });

            if (getVisibility() != View.VISIBLE) {
                showView();
            }

            Media.resetMediaScan(prefs);
        } else {
            hideView();
            binding.btnMediaDownload.setOnClickListener(null);

            if (updateMediaScan) {
                Media.updateMediaScan(prefs);
            }
        }
    }

}
