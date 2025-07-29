package org.digitalcampus.oppia.task;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONArray;
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

public class ChannelFetchTask {

    private static final String TAG = "ChannelFetchTask";

    public interface ChannelCallback {
        void onChannelsFetched(boolean showSms, boolean showWhatsapp);
        void onError(String message);
    }

    public static void execute(Context context, String url, String fullPhoneNumber, ChannelCallback callback) {
        OkHttpClient client = new OkHttpClient();
        JSONObject json = new JSONObject();

        try {
            json.put("phone_number", fullPhoneNumber);
        } catch (JSONException e) {
            Log.e(TAG, "Failed to build JSON", e);
            callback.onError("Invalid request data");
            return;
        }

        RequestBody body = RequestBody.create(json.toString(), MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder().url(url).post(body).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Network error", e);
                runOnMain(() -> callback.onError("Failed to fetch channels"));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseStr = response.body().string();
                Log.d(TAG, "Channel response: " + responseStr);

                if (response.isSuccessful()) {
                    try {
                        JSONObject json = new JSONObject(responseStr);
                        JSONArray channels = json.getJSONArray("channels");

                        boolean showSmsButton = false;
                        boolean showWhatsappButton = false;

                        for (int i = 0; i < channels.length(); i++) {
                            JSONObject ch = channels.getJSONObject(i);
                            String name = ch.getString("name");
                            String id = ch.getString("id");

                            if ("SMS".equalsIgnoreCase(name) && "sms".equalsIgnoreCase(id)) {
                                showSmsButton = true;
                            } else if ("WhatsApp".equalsIgnoreCase(name) && "whatsapp".equalsIgnoreCase(id)) {
                                showWhatsappButton = true;
                            }
                        }

                        boolean finalShowSmsButton = showSmsButton;
                        boolean finalShowWhatsappButton = showWhatsappButton;

                        runOnMain(() -> callback.onChannelsFetched(finalShowSmsButton, finalShowWhatsappButton));

                    } catch (JSONException e) {
                        Log.e(TAG, "JSON parse error", e);
                        runOnMain(() -> callback.onError("Failed to parse channel response"));
                    }
                } else {
                    runOnMain(() -> callback.onError("Error fetching channels: " + response.code()));
                }
            }

            private void runOnMain(Runnable r) {
                new Handler(Looper.getMainLooper()).post(r);
            }
        });
    }
}
