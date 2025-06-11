package org.digitalcampus.oppia.service;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.gson.JsonArray;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;

import org.digitalcampus.mobile.learning.BuildConfig;
import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.api.Paths;
import org.digitalcampus.oppia.application.App;
import org.digitalcampus.oppia.application.SessionManager;
import org.digitalcampus.oppia.listener.APIRequestFinishListener;
import org.digitalcampus.oppia.listener.APIRequestListener;
import org.digitalcampus.oppia.model.User;
import org.digitalcampus.oppia.task.APIUserRequestTask;
import org.digitalcampus.oppia.task.result.BasicResult;
import org.digitalcampus.oppia.utils.TextUtilsJava;
import org.digitalcampus.oppia.utils.ui.OppiaNotificationUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

/**
 * Background badge checker that notifies on newly awarded badges (certificates)
 */
public class BadgesChecksWorkerManager implements APIRequestListener, APIRequestFinishListener {

    public static final String TAG = BadgesChecksWorkerManager.class.getSimpleName();
    public static final String PREF_NEW_BADGES_LIST_NOTIFIED = "pref_new_badges_notified";

    private final Context context;

    @Inject
    User user;

    @Inject
    SharedPreferences prefs;

    public BadgesChecksWorkerManager(Context context) {
        this.context = context;
        initializeDaggerBase();
    }

    private void initializeDaggerBase() {
        App app = (App) context.getApplicationContext();
        app.getComponent().inject(this);
    }

    public void startBadgeCheck() {
        if (!isUserLoggedIn()) {
            Log.i(TAG, "User not logged in, skipping badge check");
            return;
        }

        // 🔁 TEMPORARY DEBUG RESET (for testing only)
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Clearing badge notification memory for debug testing");
            prefs.edit().remove(PREF_NEW_BADGES_LIST_NOTIFIED).apply();
        }

        APIUserRequestTask task = new APIUserRequestTask(context);
        task.setAPIRequestListener(this);
        task.setAPIRequestFinishListener(this, "BadgesChecksWorker");
        task.execute(Paths.SERVER_AWARDS_PATH);
    }

    private boolean isUserLoggedIn() {
        return user != null && !TextUtilsJava.isEmpty(user.getUsername());
    }

    @Override
    public void apiRequestComplete(BasicResult result) {
        if (!result.isSuccess()) {
            Log.e(TAG, "Badge API request failed");
            return;
        }

        try {
            JSONObject json = new JSONObject(result.getResultMessage());
            JSONArray objects = json.getJSONArray("objects");

            Set<String> seenBadges = prefs.getStringSet(PREF_NEW_BADGES_LIST_NOTIFIED, new HashSet<>());
            Set<String> updatedSeenBadges = new HashSet<>(seenBadges);

            int newBadgeCount = 0;

            for (int i = 0; i < objects.length(); i++) {
                JSONObject badgeJson = objects.getJSONObject(i);
                String badgeId = badgeJson.optString("certificate_pdf", "") +
                        badgeJson.optString("award_date", "") +
                        badgeJson.optString("description", "");

                if (!seenBadges.contains(badgeId)) {
                    newBadgeCount++;
                    updatedSeenBadges.add(badgeId);
                }
            }

            if (newBadgeCount > 0) {
                showNewBadgeNotification(newBadgeCount);
                prefs.edit().putStringSet(PREF_NEW_BADGES_LIST_NOTIFIED, updatedSeenBadges).apply();
            }

        } catch (JsonIOException | JSONException e) {
            Log.e(TAG, "Error parsing badge response", e);
        }
    }

//    private void showNewBadgeNotification(int badgeCount) {
//        String contentText = context.getResources().getQuantityString(
//                R.plurals.notification_new_badges_text, badgeCount, badgeCount);
//
//        NotificationCompat.Builder builder = OppiaNotificationUtils.getBaseBuilder(context, true)
//                .setContentTitle(context.getString(R.string.notification_new_badge_title))
//                .setContentText(contentText)
//                .setContentIntent(OppiaNotificationUtils.getMainActivityPendingIntent(context))
//                .setAutoCancel(true);
//
//        OppiaNotificationUtils.sendNotification(context, 8008, builder.build());
//    }

    private void showNewBadgeNotification(int badgeCount) {
        String contentText = context.getResources().getQuantityString(
                R.plurals.notification_new_badges_text, badgeCount, badgeCount);

        Intent intent = new Intent(context, org.digitalcampus.oppia.activity.MainActivity.class);
        intent.putExtra("navigate_to", "badges"); // pass a key to tell MainActivity to open BadgesFragment
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = OppiaNotificationUtils.getBaseBuilder(context, true)
                .setContentTitle(context.getString(R.string.notification_new_badge_title))
                .setContentText(contentText)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        OppiaNotificationUtils.sendNotification(context, 8008, builder.build());

    }


    @Override
    public void onRequestFinish(String idRequest) {
        Log.i(TAG, "Badge check request finished: " + idRequest);
    }

    @Override
    public void apiKeyInvalidated() {
        SessionManager.logoutCurrentUser(context);
    }
}
