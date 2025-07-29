package org.digitalcampus.oppia.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

public class OtpSmsReceiver extends BroadcastReceiver {

    public interface OtpListener {
        void onOtpReceived(String otp);
    }

    private static OtpListener listener;

    public static void setOtpListener(OtpListener otpListener) {
        listener = otpListener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if ("android.provider.Telephony.SMS_RECEIVED".equals(intent.getAction())) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                Object[] pdus = (Object[]) bundle.get("pdus");
                if (pdus != null) {
                    for (Object pdu : pdus) {
                        SmsMessage smsMessage;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            String format = bundle.getString("format");
                            smsMessage = SmsMessage.createFromPdu((byte[]) pdu, format);
                        } else {
                            smsMessage = SmsMessage.createFromPdu((byte[]) pdu);
                        }

                        String messageBody = smsMessage.getMessageBody();
                        Log.d("OtpSmsReceiver", "SMS received: " + messageBody);

                        String otp = extractOtp(messageBody);
                        if (otp != null && otp.length() == 6) {
                            if (listener != null) {
                                listener.onOtpReceived(otp);
                            }
                            abortBroadcast();
                            break;
                        }
                    }
                }
            }
        }
    }

    private String extractOtp(String message) {
        if (message == null) return null;
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\b\\d{6}\\b");
        java.util.regex.Matcher matcher = pattern.matcher(message);
        if (matcher.find()) {
            return matcher.group(0);
        }
        return null;
    }
}
