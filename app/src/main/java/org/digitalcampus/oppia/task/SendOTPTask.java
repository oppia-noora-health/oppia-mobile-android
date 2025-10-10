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

public class SendOTPTask {

    private static final String TAG = "SendOtpTask";

    public interface SendOtpCallback {
        void onSuccess();
        void onNotFound();
        void onError(String error);
    }

    public static void execute(Context context, String url, String phoneNumber, String channel, SendOtpCallback callback) {
        OkHttpClient client = new OkHttpClient();
        JSONObject json = new JSONObject();

        try {
            json.put("phone_number", phoneNumber);
            json.put("channel", channel);
            Log.d(TAG, "Sending OTP with phone: " + phoneNumber + ", channel: " + channel);
        } catch (JSONException e) {
            Log.e(TAG, "JSON creation failed", e);
            callback.onError("Invalid data format");
            return;
        }

        RequestBody body = RequestBody.create(json.toString(), MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder().url(url).post(body).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Network error", e);
                runOnMain(() -> callback.onError("Network error: " + e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseStr = response.body() != null ? response.body().string() : "";
                Log.d(TAG, "Received OTP response: " + responseStr);

                if (response.isSuccessful()) {
                    runOnMain(callback::onSuccess);
                } else {
                    Log.e(TAG, "Server returned error: " + response.code() + " body: " + responseStr);

                    try {
                        JSONObject errorJson = new JSONObject(responseStr);
                        // Many OTP APIs return { "error": "message..." }
                        String errorMessage = errorJson.optString("error", "Server error: " + response.code());

                        // If server explicitly says "not found" you can detect it here
                        if (errorMessage.toLowerCase().contains("not found")) {
                            runOnMain(callback::onNotFound);
                        } else {
                            runOnMain(() -> callback.onError(errorMessage));
                        }
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
