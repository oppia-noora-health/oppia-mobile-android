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

package org.digitalcampus.oppia.fragments;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.telephony.SmsMessage;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.hbb20.CountryCodePicker;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.mobile.learning.databinding.FragmentLoginBinding;
import org.digitalcampus.oppia.activity.WelcomeActivity;
import org.digitalcampus.oppia.api.ApiEndpoint;
import org.digitalcampus.oppia.api.Paths;
import org.digitalcampus.oppia.listener.SubmitEntityListener;
import org.digitalcampus.oppia.model.User;
import org.digitalcampus.oppia.task.LoginTask;
import org.digitalcampus.oppia.task.result.EntityResult;
import org.digitalcampus.oppia.utils.UIUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginFragment extends AppFragment implements SubmitEntityListener<User> {

    @Inject
    ApiEndpoint apiEndpoint;

    //    changed by namratha
    private static final int PERMISSION_REQUEST_CODE = 101;
    private Spinner countrySpinner;
    private Spinner languageSpinner;
    private CountryCodePicker ccp;
    private EditText phoneEditText;
    private EditText otpDigit1, otpDigit2, otpDigit3, otpDigit4, otpDigit5, otpDigit6;
    private TextView otpTimer;
    private Button sendOtpBtn;
    private Button verifyOtpBtn;
    private Button registerBtn;
    private boolean isOtpTimerFinished = false;
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private CountDownTimer countDownTimer;
    private SmsBroadcastReceiver smsBroadcastReceiver;

//    changed by namratha
//    private FragmentLoginBinding binding;
    public static LoginFragment newInstance() {
        return new LoginFragment();
    }

//    changed by namratha
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

//        changed by namratha
//        binding = FragmentLoginBinding.inflate(inflater, container, false);

        //    changed by namratha
        View view = inflater.inflate(R.layout.fragment_login_otp, container, false);

        phoneEditText = view.findViewById(R.id.register_form_phoneno_edittext);
        otpDigit1 = view.findViewById(R.id.otp_digit_1);
        otpDigit2 = view.findViewById(R.id.otp_digit_2);
        otpDigit3 = view.findViewById(R.id.otp_digit_3);
        otpDigit4 = view.findViewById(R.id.otp_digit_4);
        otpDigit5 = view.findViewById(R.id.otp_digit_5);
        otpDigit6 = view.findViewById(R.id.otp_digit_6);
        otpTimer = view.findViewById(R.id.text_otp_timer);
        sendOtpBtn = view.findViewById(R.id.btn_send_otp);
        verifyOtpBtn = view.findViewById(R.id.btn_verify_otp);
        registerBtn = view.findViewById(R.id.action_register_btn);
        countrySpinner = view.findViewById(R.id.spinner_country);
        languageSpinner = view.findViewById(R.id.spinner_language);
        ccp = view.findViewById(R.id.ccp);
        setupCountryAndLanguageSpinners();

//        return binding.getRoot();
        return view;
    }


//    changed by namratha
//    @Override
//    public void onActivityCreated(Bundle savedInstanceState) {
//        super.onActivityCreated(savedInstanceState);
//        getAppComponent().inject(this);
//
//        binding.loginBtn.setOnClickListener(v -> onLoginClick());
//
//        binding.btnResetPassword.setOnClickListener(v -> {
//            WelcomeActivity wa = (WelcomeActivity) getActivity();
//            wa.switchTab(WelcomeActivity.TAB_RESET_PASSWORD);
//        });
//        binding.actionRegisterBtn.setOnClickListener(v -> {
//            WelcomeActivity wa = (WelcomeActivity) getActivity();
//            wa.switchTab(WelcomeActivity.TAB_REGISTER);
//        });
//
//        binding.btnRememberUsername.setOnClickListener(v -> {
//            WelcomeActivity wa = (WelcomeActivity) getActivity();
//            wa.switchTab(WelcomeActivity.TAB_REMEMBER_USERNAME);
//        });
//
//    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getAppComponent().inject(this);

        //    changed by namratha
        sendOtpBtn.setOnClickListener(v -> {
            if (checkAndRequestPermissions()) {
                ccp.registerCarrierNumberEditText(phoneEditText);
                String mobile = phoneEditText.getText().toString().trim();
                if ((mobile.length() > 0) && !ccp.isValidFullNumber()) {
                    UIUtils.showAlert(getActivity(), R.string.error, R.string.error_invalid_phone);
                    return;
                }
                String fullNumber = ccp.getFullNumberWithPlus();

                if (isOtpTimerFinished) {
                    // Timer ended: ask for channel
                    fetchChannelsAndShowDialog(fullNumber);
                } else {
                    // Default SMS
                    sendOtp();
                }
            }
        });

        verifyOtpBtn.setOnClickListener(v -> verifyandloginOtp());

        registerBtn.setOnClickListener(v -> {
            WelcomeActivity wa = (WelcomeActivity) getActivity();
            wa.switchTab(WelcomeActivity.TAB_REGISTER);
        });

        // Register SMS BroadcastReceiver
        smsBroadcastReceiver = new SmsBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
        requireActivity().registerReceiver(smsBroadcastReceiver, intentFilter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        //    changed by namratha
        if (smsBroadcastReceiver != null) {
            requireActivity().unregisterReceiver(smsBroadcastReceiver);
            smsBroadcastReceiver = null;
        }
    }

    //    changed by namratha
    private boolean checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            List<String> permissionsNeeded = new ArrayList<>();

            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.RECEIVE_SMS);
            }
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.READ_SMS);
            }

            if (!permissionsNeeded.isEmpty()) {
                requestPermissions(permissionsNeeded.toArray(new String[0]), PERMISSION_REQUEST_CODE);
                return false;
            }
        }
        return true;
    }

    //    changed by namratha
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (allGranted) {
                Toast.makeText(getActivity(), "Permissions granted. Please try again.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), "Permissions denied. Cannot auto-fill OTP.", Toast.LENGTH_LONG).show();
            }
        }
    }

    //    changed by namratha
    private class SmsBroadcastReceiver extends BroadcastReceiver {
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
                            Log.d("LoginFragment", "SMS received: " + messageBody);

                            // Extract 6-digit OTP from the message body
                            String otp = extractOtp(messageBody);
                            if (otp != null && otp.length() == 6) {
                                autofillOtp(otp);
                                // Optionally cancel broadcast so other apps don't intercept
                                abortBroadcast();
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    //    changed by namratha
    private String extractOtp(String message) {
        if (message == null) return null;
        // Simple regex to find first 6-digit number in SMS
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\b\\d{6}\\b");
        java.util.regex.Matcher matcher = pattern.matcher(message);
        if (matcher.find()) {
            return matcher.group(0);
        }
        return null;
    }

    //    changed by namratha
    private void autofillOtp(String otp) {
        // Run on UI thread
        requireActivity().runOnUiThread(() -> {
            if (otp.length() == 6) {
                otpDigit1.setText(String.valueOf(otp.charAt(0)));
                otpDigit2.setText(String.valueOf(otp.charAt(1)));
                otpDigit3.setText(String.valueOf(otp.charAt(2)));
                otpDigit4.setText(String.valueOf(otp.charAt(3)));
                otpDigit5.setText(String.valueOf(otp.charAt(4)));
                otpDigit6.setText(String.valueOf(otp.charAt(5)));
            }
        });
    }

    //    changed by namratha
    private void sendOtp() {
        ccp.registerCarrierNumberEditText(phoneEditText);

        String phoneNo = phoneEditText.getText().toString().trim();
        if ((phoneNo.length() > 0) && !ccp.isValidFullNumber()) {
            UIUtils.showAlert(getActivity(), R.string.error, R.string.error_invalid_phone);
            phoneEditText.requestFocus();
            return;
        }

        String fullNumber = ccp.getFullNumberWithPlus();

        phoneEditText.setEnabled(false);
        sendOtpBtn.setEnabled(false);

        sendOtpWithChannel(fullNumber, "sms");
    }

    //    changed by namratha
    private void fetchChannelsAndShowDialog(String fullPhoneNumber) {
        String url = apiEndpoint.getFullURL(requireContext(), Paths.CHANNEL_PATH);
        OkHttpClient client = new OkHttpClient();
        JSONObject json = new JSONObject();
        try {
            json.put("phone_number", fullPhoneNumber);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }
        RequestBody body = RequestBody.create(json.toString(), JSON);
        Request request = new Request.Builder().url(url).post(body).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getActivity(), "Failed to fetch channels", Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseStr = response.body().string();
                    try {
                        JSONObject json = new JSONObject(responseStr);
                        JSONArray channels = json.getJSONArray("channels");
                        List<String> channelNames = new ArrayList<>();
                        List<String> channelIds = new ArrayList<>();
                        for (int i = 0; i < channels.length(); i++) {
                            JSONObject ch = channels.getJSONObject(i);
                            channelNames.add(ch.getString("name"));
                            channelIds.add(ch.getString("id"));
                        }
                        requireActivity().runOnUiThread(() ->
                                showChannelSelectionDialog(fullPhoneNumber, channelNames, channelIds));
                    } catch (JSONException e) {
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(getActivity(), "Failed to parse channel response", Toast.LENGTH_SHORT).show()
                        );
                    }
                } else {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(getActivity(), "Error fetching channels", Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });
    }

    //    changed by namratha
    private void showChannelSelectionDialog(String fullPhoneNumber, List<String> channelNames, List<String> channelIds) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Choose Channel");
        CharSequence[] namesArray = channelNames.toArray(new CharSequence[0]);
        builder.setItems(namesArray, (dialog, which) -> {
            String selectedChannel = channelIds.get(which);
            sendOtpWithChannel(fullPhoneNumber, selectedChannel);
        });
        builder.show();
    }

    //    changed by namratha
    private void sendOtpWithChannel(String fullNumber, String channel) {
        String url = apiEndpoint.getFullURL(requireContext(), Paths.SEND_OTP_PATH);
        OkHttpClient client = new OkHttpClient();

        JSONObject json = new JSONObject();
        try {
            json.put("phone_number", fullNumber);
            json.put("channel", channel);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        RequestBody body = RequestBody.create(json.toString(), JSON);
        Request request = new Request.Builder().url(url).post(body).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(getActivity(), "Failed to send OTP", Toast.LENGTH_SHORT).show();
                    phoneEditText.setEnabled(true);
                    sendOtpBtn.setEnabled(true);
                });
            }

            @Override
            public void onResponse(Call call, Response response) {
                requireActivity().runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        Toast.makeText(getActivity(), "OTP sent", Toast.LENGTH_SHORT).show();
                        startOtpCountdown();
                    } else {
                        Toast.makeText(getActivity(), "Phone number not found.", Toast.LENGTH_SHORT).show();
                        Toast.makeText(getActivity(), "Please contact your nearest Noora Health team member for Assistance.", Toast.LENGTH_SHORT).show();
                        phoneEditText.setEnabled(true);
                        sendOtpBtn.setEnabled(true);
                    }
                });
            }
        });
    }

    //    changed by namratha
    private void startOtpCountdown() {
        otpTimer.setVisibility(View.VISIBLE);
        phoneEditText.setEnabled(false);
        sendOtpBtn.setEnabled(false);
        isOtpTimerFinished = false;

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        countDownTimer = new CountDownTimer(60000, 1000) {
            public void onTick(long millisUntilFinished) {
                int seconds = (int) (millisUntilFinished / 1000);
                otpTimer.setText(String.format("Resend otp in 00:%02d", seconds));
            }

            public void onFinish() {
                otpTimer.setText("Resend otp in 00:00");
                phoneEditText.setEnabled(true);
                sendOtpBtn.setEnabled(true);  // Enable Send OTP after timer
                isOtpTimerFinished = true;    // Mark timer as finished
            }
        }.start();
    }

    //    changed by namratha
    private void verifyandloginOtp() {
        String mobile = phoneEditText.getText().toString().trim();
        String otp = otpDigit1.getText().toString().trim()
                + otpDigit2.getText().toString().trim()
                + otpDigit3.getText().toString().trim()
                + otpDigit4.getText().toString().trim()
                + otpDigit5.getText().toString().trim()
                + otpDigit6.getText().toString().trim();

        if (mobile.isEmpty()) {
            Toast.makeText(getActivity(), "Please enter your phone number", Toast.LENGTH_SHORT).show();
            phoneEditText.requestFocus();
            return;
        }

        if (!ccp.isValidFullNumber()) {
            UIUtils.showAlert(getActivity(), R.string.error, R.string.error_invalid_phone);
            phoneEditText.requestFocus();
            return;
        }

        if (otp.length() != 6) {
            UIUtils.showAlert(getActivity(), R.string.error, R.string.error_invalid_otp);
            otpDigit1.requestFocus();
            return;
        }

        showProgressDialog(getString(R.string.login_process));

        User user = new User();
        user.setUsername(ccp.getFullNumberWithPlus());
        user.setPhoneNo(ccp.getFullNumberWithPlus());

        LoginTask lt = new LoginTask(getActivity(), apiEndpoint);
        lt.setOtpCode(otp);
        lt.setLoginListener(this);
        lt.execute(user);
    }

    //    changed by namratha
//    protected void onLoginClick() {
//        String username = binding.loginUsernameField.getText().toString();
//        //check valid email address format
//        if (username.length() == 0) {
//            UIUtils.showAlert(super.getActivity(), R.string.error, R.string.error_no_username);
//            return;
//        }
//
//        String password = binding.loginPasswordField.getText().toString();
//
//        showProgressDialog(getString(R.string.login_process));
//
//        User user = new User();
//        user.setUsername(username);
//        user.setPassword(password);
//
//        LoginTask lt = new LoginTask(super.getActivity(), apiEndpoint);
//        lt.setLoginListener(this);
//        lt.execute(user);
//    }


    public void submitComplete(EntityResult<User> response) {
        hideProgressDialog();

        if (response.isSuccess()) {
            User user = response.getEntity();
            ((WelcomeActivity) getActivity()).onSuccessUserAccess(user, true);

        } else {
            Context ctx = super.getActivity();
            if (ctx != null) {
                UIUtils.showAlert(ctx, R.string.title_login, response.getResultMessage());
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        hideProgressDialog();
        //    changed by namratha
        if (countDownTimer != null) countDownTimer.cancel();
    }

    //    changed by namratha
    private void setupCountryAndLanguageSpinners() {
        String[] countries = {"India", "Indonesia", "Bangladesh", "Nepal"};
        ArrayAdapter<String> countryAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, countries);
        countryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        countrySpinner.setAdapter(countryAdapter);

        countrySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedCountry = countries[position];
                List<String> languages = new ArrayList<>();
                switch (selectedCountry) {
                    case "India":
                        ccp.setCountryForNameCode("IN");
                        languages = Arrays.asList("English");
                        break;
                    case "Indonesia":
                        ccp.setCountryForNameCode("ID");
                        languages = Arrays.asList("English");
                        break;
                    case "Bangladesh":
                        ccp.setCountryForNameCode("BD");
                        languages = Arrays.asList("English", "Bangla");
                        break;
                    case "Nepal":
                        ccp.setCountryForNameCode("NP");
                        languages = Arrays.asList("English", "Nepali");
                        break;
                }
                ArrayAdapter<String> languageAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, languages);
                languageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                languageSpinner.setAdapter(languageAdapter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }


}
