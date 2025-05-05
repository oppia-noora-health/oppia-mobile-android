package org.digitalcampus.oppia.utils.resources;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.CourseActivity;
import org.digitalcampus.oppia.activity.DownloadMediaActivity;
import org.digitalcampus.oppia.holder.CourseHolder;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.utils.storage.Storage;

import java.io.File;

public class JSInterfaceForAudioPlayer extends JSInterface {

    private static final String TAG = JSInterfaceForAudioPlayer.class.getSimpleName();

    //Name of the JS interface to add to the webView
    public static final String INTERFACE_EXPOSED_NAME = "OppiaAndroid_AudioPlayer";
    private static final String JS_RESOURCE_FILE = "observe_audio_player.js";
    public static final String PREF_RETRY_AUDIO = "pref_retry_audio";

    private int audioDuration;
    private OnPlayButtonClickListener onPlayButtonClickListener;
    private boolean playing = false;
    private Course course; // Add course variable

    public interface OnPlayButtonClickListener {
        void onPlayButtonClick(boolean playing, int duration);
        void onAudioCompleted(String filename);
    }

    public void setOnPlayButtonClickListener(OnPlayButtonClickListener listener) {
        this.onPlayButtonClickListener = listener;
    }

    public JSInterfaceForAudioPlayer(Context ctx, Course course) {
        super(ctx);
        this.course = course;
        loadJSInjectionSourceFile(JS_RESOURCE_FILE);
    }

    @Override
    public String getInterfaceExposedName() {
        return INTERFACE_EXPOSED_NAME;
    }

    @JavascriptInterface   // must be added for API 17 or higher
    public void onAudioCompleted(String audioSource) {
        if (onPlayButtonClickListener != null) {
            File audioFile = new File(audioSource);
            onPlayButtonClickListener.onAudioCompleted(audioFile.getName());
        }
    }


    @JavascriptInterface   // must be added for API 17 or higher
    public void onPlayButtonClick(String audioSource) {
        Log.d(TAG, "onPlayButtonClick called with source: " + audioSource);
        File audioFile = new File(audioSource);
        Log.d(TAG, "Audio file exists: " + audioFile.exists());

        // ✅ Check if file exists before proceeding
        if (!Storage.mediaFileExists(context, Uri.decode(audioFile.getName()))){
//        if (!audioFile.exists()) {
//            Toast.makeText(context, "Please download audio", Toast.LENGTH_SHORT).show();

            // 🔁 Redirect to download media page
            Intent intent = new Intent(context, DownloadMediaActivity.class);
            intent.putExtra(DownloadMediaActivity.MISSING_MEDIA_COURSE_FILTER, getCourse());
            context.startActivity(intent);
            return;
        }

//        if (!audioFile.exists()) {
//            context.getSharedPreferences("oppia", Context.MODE_PRIVATE)
//                    .edit()
//                    .putString(PREF_RETRY_AUDIO, audioSource)
//                    .apply();
//
//            Intent intent = new Intent(context, DownloadMediaActivity.class);
//            intent.putExtra(DownloadMediaActivity.MISSING_MEDIA_COURSE_FILTER, getCourse());
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // Important
//            context.startActivity(intent);
//            return;
//        }

        // Get duration only once
        if (audioDuration == 0) {
            audioDuration = calculateAudioDuration(audioSource);
        }

        if (audioDuration > 0) {
            playing = !playing;
            if (onPlayButtonClickListener != null) {
                onPlayButtonClickListener.onPlayButtonClick(playing, audioDuration);
            }
        }

//        if (audioDuration > 0 && onPlayButtonClickListener != null) {
//            onPlayButtonClickListener.onPlayButtonClick(false, audioDuration); // false = don't resume
//        }
    }

    private Course getCourse() {
        return CourseHolder.getCourse(); // Assumes Course is set in CourseHolder, just like PageWidget
    }


    // Function to make the download button visible
//    private void showDownloadButton() {
//        if (context instanceof android.app.Activity) {
//            android.app.Activity activity = (android.app.Activity) context;
//            activity.runOnUiThread(() -> {
//                View downloadButton = activity.findViewById(R.id.download_course);
//                if (downloadButton != null) {
//                    downloadButton.setVisibility(View.VISIBLE);
//                    downloadButton.setOnClickListener(v -> {
//                        Intent i = new Intent(context, DownloadMediaActivity.class);
//                        Bundle tb = new Bundle();
//                        tb.putSerializable(DownloadMediaActivity.MISSING_MEDIA_COURSE_FILTER, course);
//                        i.putExtras(tb);
//                        context.startActivity(i);
//                    });
//                }
//            });
//        }
//    }

    private int calculateAudioDuration(String audioSource) {
        MediaPlayer mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(audioSource);
            mediaPlayer.prepare();
            int duration = mediaPlayer.getDuration();
            mediaPlayer.release();
            return duration;
        } catch (Exception e) {
            Log.e(TAG, "calculateAudioDuration: ", e);;
            return 0;
        }
    }
}