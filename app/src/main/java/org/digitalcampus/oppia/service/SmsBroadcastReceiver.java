package org.digitalcampus.oppia.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.Status;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SmsBroadcastReceiver extends BroadcastReceiver {

    public interface SmsListener {
        void onOtpReceived(String otp);
    }

    private final SmsListener listener;

    public SmsBroadcastReceiver(SmsListener listener) {
        this.listener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (SmsRetriever.SMS_RETRIEVED_ACTION.equals(intent.getAction())) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                Status status = (Status) extras.get(SmsRetriever.EXTRA_STATUS);
                if (status != null) {
                    switch (status.getStatusCode()) {
                        case CommonStatusCodes.SUCCESS:
                            // Retrieve the SMS message
                            String message = (String) extras.get(SmsRetriever.EXTRA_SMS_MESSAGE);
                            Log.d("SmsBroadcastReceiver", "SMS Message Received: " + message);

                            // Extract 6-digit OTP using regex
                            Pattern pattern = Pattern.compile("\\d{6}");
                            Matcher matcher = pattern.matcher(message);
                            if (matcher.find()) {
                                String otp = matcher.group(0);
                                Log.d("SmsBroadcastReceiver", "Extracted OTP: " + otp);
                                if (listener != null) {
                                    listener.onOtpReceived(otp);
                                }
                            } else {
                                Log.w("SmsBroadcastReceiver", "No OTP found in message");
                            }
                            break;

                        case CommonStatusCodes.TIMEOUT:
                            Log.w("SmsBroadcastReceiver", "SMS Retriever timed out (no SMS received)");
                            break;

                        default:
                            Log.w("SmsBroadcastReceiver", "Unknown status code: " + status.getStatusCode());
                            break;
                    }
                }
            }
        }
    }
}
