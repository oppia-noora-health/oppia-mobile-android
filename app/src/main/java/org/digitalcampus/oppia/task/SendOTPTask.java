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
            Log.d("sendotp",phoneNumber+channel);
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
                runOnMain(() -> callback.onError("Failed to send OTP"));
            }

            @Override
            public void onResponse(Call call, Response response) {
                runOnMain(() -> {
                    if (response.isSuccessful()) {
                        callback.onSuccess();
                    } else {
                        callback.onNotFound();
                    }
                });
            }

            private void runOnMain(Runnable r) {
                new Handler(Looper.getMainLooper()).post(r);
            }
        });
    }
}
