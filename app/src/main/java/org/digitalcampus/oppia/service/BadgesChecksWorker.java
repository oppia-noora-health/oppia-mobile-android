package org.digitalcampus.oppia.service;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;
import androidx.work.impl.utils.futures.SettableFuture;

import com.google.common.util.concurrent.ListenableFuture;

import org.digitalcampus.mobile.learning.BuildConfig;

@SuppressLint("RestrictedApi")
public class BadgesChecksWorker extends ListenableWorker {

    public static final String TAG = BadgesChecksWorker.class.getSimpleName();
    private SettableFuture<Result> future;

    public BadgesChecksWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public ListenableFuture<Result> startWork() {
        Log.i(TAG, "startWork: BadgesChecksWorker");

        future = SettableFuture.create();

        if (BuildConfig.DEBUG) {
            getApplicationContext()
                    .getSharedPreferences("org.digitalcampus.oppia_preferences", Context.MODE_PRIVATE)
                    .edit()
                    .remove("pref_new_badges_notified")
                    .apply();
        }

        BadgesChecksWorkerManager manager = new BadgesChecksWorkerManager(getApplicationContext());
        manager.startBadgeCheck();

        future.set(Result.success());

        return future;
    }

    @Override
    public void onStopped() {
        super.onStopped();
        Log.i(TAG, "BadgesChecksWorker stopped");
    }
}
