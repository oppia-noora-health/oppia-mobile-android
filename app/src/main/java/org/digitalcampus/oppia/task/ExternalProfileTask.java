package org.digitalcampus.oppia.task;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ExternalProfileTask {

    private static final String TAG = "ExternalProfileTask";  // For logging

    public interface ExternalProfileCallback {
        void onSuccess(JSONObject fullResponse);
        void onNotFound();
        void onError(String error);
    }

    public static void execute(Context context, String url, String fullPhoneNumber, String country, String language, ExternalProfileCallback callback) {
        OkHttpClient client = new OkHttpClient();
        JSONObject json = new JSONObject();

        try {
            json.put("phone_number", fullPhoneNumber);
            json.put("country", country);
            json.put("language", language);
            Log.d(TAG, "External profile phone: " + fullPhoneNumber );
        } catch (JSONException e) {
            Log.e(TAG, "JSON Exception while building request", e);
            callback.onError("Failed to build request");
            return;
        }

        RequestBody body = RequestBody.create(json.toString(), MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder().url(url).post(body).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Network error while calling external profile", e);
                runOnMain(() -> callback.onError("Network error: " + e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseStr = response.body() != null ? response.body().string() : "";
                Log.d(TAG, "Received response: " + responseStr);

                if (response.isSuccessful()) {
                    try {
                        JSONObject json = new JSONObject(responseStr);
                        if (json.has("status") && "success".equalsIgnoreCase(json.getString("status"))) {
                            runOnMain(() -> callback.onSuccess(json));
                        } else {
                            Log.w(TAG, "Profile not found in response");
                            runOnMain(callback::onNotFound);
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "JSON parsing error", e);
                        runOnMain(() -> callback.onError("Invalid JSON"));
                    }
                } else {
                    Log.e(TAG, "Server returned error: " + response.code() + " body: " + responseStr);

                    try {
                        JSONObject errorJson = new JSONObject(responseStr);
                        // Try to extract the "error" field if present
                        String errorMessage = errorJson.optString("error", "Server error: " + response.code());
                        runOnMain(() -> callback.onError(errorMessage));
                    } catch (JSONException e) {
                        runOnMain(() -> callback.onError("Server error: " + response.code()));
                    }
                }
            }

            private void runOnMain(Runnable r) {
                new Handler(Looper.getMainLooper()).post(r);
            }
        });
    }
}
