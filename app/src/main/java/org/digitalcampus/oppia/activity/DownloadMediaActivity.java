/*
 * This file is part of OppiaMobile - https://digital-campus.org/
 *
 * OppiaMobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OppiaMobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OppiaMobile. If not, see <http://www.gnu.org/licenses/>.
 */

package org.digitalcampus.oppia.activity;

import android.animation.ValueAnimator;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.widget.Toast;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.mobile.learning.databinding.ActivityDownloadMediaBinding;
import org.digitalcampus.oppia.adapter.DownloadMediaAdapter;
import org.digitalcampus.oppia.listener.DownloadMediaListener;
import org.digitalcampus.oppia.listener.ScanMediaListener;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.CoursesRepository;
import org.digitalcampus.oppia.model.Media;
import org.digitalcampus.oppia.service.DownloadBroadcastReceiver;
import org.digitalcampus.oppia.service.DownloadService;
import org.digitalcampus.oppia.service.DownloadServiceDelegate;
import org.digitalcampus.oppia.task.ScanMediaTask;
import org.digitalcampus.oppia.task.result.EntityListResult;
import org.digitalcampus.oppia.utils.ConnectionUtils;
import org.digitalcampus.oppia.utils.MultiChoiceHelper;
import org.digitalcampus.oppia.utils.UIUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

public class DownloadMediaActivity extends AppActivity implements DownloadMediaListener, ScanMediaListener {

    public static final String MISSING_MEDIA_COURSE_FILTER = "missing_media_course_filter";

    private ArrayList<Media> missingMedia;
    private DownloadBroadcastReceiver receiver;
    private boolean isSortByCourse;
    private ArrayList<Media> mediaSelected;
    private DownloadMediaAdapter adapterMedia;
    private MultiChoiceHelper multiChoiceHelper;
    private ActivityDownloadMediaBinding binding;

    public enum DownloadMode {INDIVIDUALLY, DOWNLOAD_ALL, STOP_ALL}

    @Inject
    DownloadServiceDelegate downloadServiceDelegate;
    @Inject
    CoursesRepository coursesRepository;

    @Override
    public void onStart() {
        super.onStart();
        initialize();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDownloadMediaBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());
        getAppComponent().inject(this);

        missingMedia = new ArrayList<>();
        mediaSelected = new ArrayList<>();

        configureAdapterMedia();
        binding.missingMediaList.setAdapter(adapterMedia);

        Media.resetMediaScan(prefs);

        scanMissingMedia();
    }

    private void configureAdapterMedia() {

        adapterMedia = new DownloadMediaAdapter(this, missingMedia);
        multiChoiceHelper = new MultiChoiceHelper(this, adapterMedia);
        multiChoiceHelper.setMultiChoiceModeListener(new MultiChoiceHelper.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(androidx.appcompat.view.ActionMode mode, int position, long id, boolean checked) {
                Log.v(TAG, "Count: " + multiChoiceHelper.getCheckedItemCount());
                if (checked) {
                    mediaSelected.add(missingMedia.get(position));
                } else {
                    mediaSelected.remove(missingMedia.get(position));
                }

                int count = mediaSelected.size();
                mode.setSubtitle(count == 1 ? count + " item selected" : count + " items selected");

                for (Media m : mediaSelected) {
                    if (!m.isDownloading()) {
                        binding.downloadSelected.setText(getString(R.string.missing_media_download_selected));
                        break;
                    }
                }
            }

            @Override
            public boolean onCreateActionMode(final androidx.appcompat.view.ActionMode mode, Menu menu) {

                onPrepareOptionsMenu(menu);
                mode.setTitle(R.string.title_download_media);

                if (binding.homeMessages.getVisibility() != View.VISIBLE) {
                    binding.homeMessages.setVisibility(View.VISIBLE);
                    binding.downloadSelected.setOnClickListener(v -> {
                        DownloadMode downloadMode = binding.downloadSelected.getText()
                                .equals(getString(R.string.missing_media_download_selected)) ? DownloadMode.DOWNLOAD_ALL
                                : DownloadMode.STOP_ALL;
                        binding.downloadSelected.setText(binding.downloadSelected.getText()
                                .equals(getString(R.string.missing_media_download_selected)) ? getString(R.string.missing_media_stop_selected)
                                : getString(R.string.missing_media_download_selected));

                        for (Media m : mediaSelected) {
                            downloadMedia(m, downloadMode);
                        }

                        mode.finish();
                    });

                    showDownloadMediaMessage();
                }

                adapterMedia.setEnterOnMultiChoiceMode(true);
                adapterMedia.notifyDataSetChanged();
                binding.downloadSelected.setText(getString(R.string.missing_media_stop_selected));

                menu.findItem(R.id.menu_sort_by).setVisible(false);

                return true;
            }

            @Override
            public boolean onPrepareActionMode(androidx.appcompat.view.ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(androidx.appcompat.view.ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_select_all:
                        for (int i = 0; i < adapterMedia.getItemCount(); i++) {
                            if (!multiChoiceHelper.isItemChecked(i)) {
                                multiChoiceHelper.setItemChecked(i, true, true);
                            }
                        }
                        return true;

                    case R.id.menu_unselect_all:
                        mode.finish();
                        return true;

                    default:
                        return false;
                }
            }

            @Override
            public void onDestroyActionMode(androidx.appcompat.view.ActionMode mode) {

                mediaSelected.clear();
                hideDownloadMediaMessage();
                adapterMedia.setEnterOnMultiChoiceMode(false);
                adapterMedia.notifyDataSetChanged();
                multiChoiceHelper.clearChoices();

                mode.getMenu().findItem(R.id.menu_sort_by).setVisible(true);
            }
        });

        adapterMedia.setMultiChoiceHelper(multiChoiceHelper);
        adapterMedia.sortByFilename();

        adapterMedia.setOnItemClickListener(position -> {
            Log.d(TAG, "Clicked " + position);
            Media mediaToDownload = missingMedia.get(position);

            downloadMedia(mediaToDownload, DownloadMode.INDIVIDUALLY);
        });
    }

    private void scanMissingMedia() {

        binding.progressMedia.setVisibility(View.VISIBLE);

        List<Course> courses;
        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null) {
            Course course = (Course) bundle.getSerializable(MISSING_MEDIA_COURSE_FILTER);
            courses = Arrays.asList(course);
        } else {
            courses = coursesRepository.getCourses(this);
        }

        ScanMediaTask task = new ScanMediaTask(this);
        task.setScanMediaListener(this);
        task.execute(courses);
    }


    @Override
    public void onResume() {
        super.onResume();
        if (missingMedia != null && !missingMedia.isEmpty()) {
            // We already have loaded media (coming from orientation change)
            isSortByCourse = false;
            adapterMedia.notifyDataSetChanged();
            binding.emptyState.setVisibility(View.GONE);
        } else {
            if (binding.progressMedia.getVisibility() == View.GONE) {
                binding.emptyState.setVisibility(View.VISIBLE);
            }
        }

        receiver = new DownloadBroadcastReceiver();
        receiver.setMediaListener(this);
        IntentFilter broadcastFilter = new IntentFilter(DownloadService.BROADCAST_ACTION);
        broadcastFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        ContextCompat.registerReceiver(this, receiver, broadcastFilter, ContextCompat.RECEIVER_NOT_EXPORTED);

        invalidateOptionsMenu();
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        ArrayList<Media> savedMissingMedia = (ArrayList<Media>) savedInstanceState.getSerializable(TAG);
        this.missingMedia.clear();
        this.missingMedia.addAll(savedMissingMedia);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putSerializable(TAG, missingMedia);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        getMenuInflater().inflate(R.menu.missing_media_sortby, menu);
        MenuItem selectAll = menu.findItem(R.id.menu_unselect_all);
        if (selectAll != null) {
            selectAll.setVisible(!missingMedia.isEmpty());
        }

        MenuItem sortBy = menu.findItem(R.id.menu_sort_by);
        if (sortBy != null) {
            sortBy.setVisible(!missingMedia.isEmpty());
            sortBy.setTitle(isSortByCourse ? getString(R.string.menu_sort_by_filename)
                    : getString(R.string.menu_sort_by_course));
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.menu_sort_by:
                if (isSortByCourse) {
                    adapterMedia.sortByFilename();
                    isSortByCourse = false;
                } else {
                    adapterMedia.sortByCourse();
                    isSortByCourse = true;
                invalidateOptionsMenu();
                }
                return true;
            case R.id.menu_select_all:
                for (int i = 0; i < adapterMedia.getItemCount(); i++) {
                    if (!multiChoiceHelper.isItemChecked(i)) {
                        multiChoiceHelper.setItemChecked(i, true, true);
                    }
                }
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onDownloadProgress(String fileUrl, int progress) {
        Media mediaFile = findMedia(fileUrl);
        if (mediaFile != null) {
            mediaFile.setProgress(progress);
            adapterMedia.notifyDataSetChanged();
        }
    }

    @Override
    public void onDownloadFailed(String fileUrl, String message) {
        Media mediaFile = findMedia(fileUrl);
        if (mediaFile != null) {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();

            mediaFile.setDownloading(false);
            mediaFile.setFailed(true);
            mediaFile.setProgress(0);
            adapterMedia.notifyDataSetChanged();
        }
    }

    @Override
    public void onDownloadComplete(String fileUrl) {
        Media mediaFile = findMedia(fileUrl);
        if (mediaFile != null) {
            Toast.makeText(this, this.getString(R.string.download_complete), Toast.LENGTH_LONG).show();

            missingMedia.remove(mediaFile);
            adapterMedia.notifyDataSetChanged();
            binding.emptyState.setVisibility((missingMedia.isEmpty()) ? View.VISIBLE : View.GONE);
            invalidateOptionsMenu();
        }
    }

    private Media findMedia(String fileUrl) {
        if (!missingMedia.isEmpty()) {
            for (Media mediaFile : missingMedia) {
                if (mediaFile.getDownloadUrl().equals(fileUrl)) {
                    return mediaFile;
                }
            }
        }
        return null;
    }

    private void downloadMedia(Media mediaToDownload, DownloadMode mode) {
        if (!ConnectionUtils.isOnWifi(DownloadMediaActivity.this) && !prefs.getBoolean(PrefsActivity.PREF_BACKGROUND_DATA_CONNECT, false)) {
            UIUtils.showAlert(DownloadMediaActivity.this, R.string.warning, R.string.warning_wifi_required);
            return;
        }

        if (!mediaToDownload.isDownloading()) {
            if (mode.equals(DownloadMode.DOWNLOAD_ALL) ||
                    mode.equals(DownloadMode.INDIVIDUALLY)) {
                startDownload(mediaToDownload);
            }
        } else {
            if (mode.equals(DownloadMode.STOP_ALL) ||
                    mode.equals(DownloadMode.INDIVIDUALLY)) {
                stopDownload(mediaToDownload);
            }
        }


    }

    private void startDownload(Media mediaToDownload) {

        downloadServiceDelegate.startDownload(this, mediaToDownload);

        mediaToDownload.setDownloading(true);
        mediaToDownload.setProgress(0);
        adapterMedia.notifyDataSetChanged();

        binding.downloadSelected.setText(getString(R.string.missing_media_download_selected));
        for (Media m : mediaSelected) {
            if (m.isDownloading()) {
                binding.downloadSelected.setText(getString(R.string.missing_media_stop_selected));
                break;
            }
        }
    }

    private void stopDownload(Media mediaToDownload) {

        downloadServiceDelegate.stopDownload(this, mediaToDownload);

        mediaToDownload.setDownloading(false);
        mediaToDownload.setProgress(0);
        adapterMedia.notifyDataSetChanged();

        for (Media m : mediaSelected) {
            if (!m.isDownloading()) {
                binding.downloadSelected.setText(getString(R.string.missing_media_download_selected));
                break;
            }
        }
    }

    private void showDownloadMediaMessage() {
        TranslateAnimation anim = new TranslateAnimation(0, 0, -200, 0);
        anim.setDuration(900);
        binding.homeMessages.startAnimation(anim);

        binding.homeMessages.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ValueAnimator animator = ValueAnimator.ofInt(0, binding.homeMessages.getMeasuredHeight());
        //@Override
        animator.addUpdateListener(
                valueAnimator -> binding.missingMediaList.setPaddingRelative(0, (Integer) valueAnimator.getAnimatedValue(), 0, 0));
        animator.setStartDelay(200);
        animator.setDuration(700);
        animator.start();
    }

    private void hideDownloadMediaMessage() {

        TranslateAnimation anim = new TranslateAnimation(0, 0, 0, -200);
        anim.setDuration(900);
        binding.homeMessages.startAnimation(anim);

        binding.homeMessages.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ValueAnimator animator = ValueAnimator.ofInt(binding.homeMessages.getMeasuredHeight(), 0);
        //@Override
        animator.addUpdateListener(
                valueAnimator -> binding.missingMediaList.setPaddingRelative(0, (Integer) valueAnimator.getAnimatedValue(), 0, 0));
        animator.setStartDelay(0);
        animator.setDuration(700);
        animator.start();


        binding.homeMessages.setVisibility(View.GONE);
    }


    // MISSING MEDIA SCAN
    @Override
    public void scanStart() {
    }

    @Override
    public void scanProgressUpdate(String msg) {

    }

    @Override
    public void scanComplete(EntityListResult<Media> result) {

        binding.progressMedia.setVisibility(View.GONE);

        binding.emptyState.setVisibility(result.getEntityList().isEmpty() ? View.VISIBLE : View.GONE);

        missingMedia.clear();
        missingMedia.addAll(result.getEntityList());
        adapterMedia.notifyDataSetChanged();
    }
}
