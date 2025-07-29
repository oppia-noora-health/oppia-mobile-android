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

import static org.digitalcampus.oppia.holder.ActivityHolder.getActivity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
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
import androidx.core.content.ContextCompat;

import com.hbb20.CountryCodePicker;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.WelcomeActivity;
import org.digitalcampus.oppia.api.ApiEndpoint;
import org.digitalcampus.oppia.api.Paths;
import org.digitalcampus.oppia.listener.SubmitEntityListener;
import org.digitalcampus.oppia.model.User;
import org.digitalcampus.oppia.service.OtpSmsReceiver;
import org.digitalcampus.oppia.task.ChannelFetchTask;
import org.digitalcampus.oppia.task.ExternalProfileTask;
import org.digitalcampus.oppia.task.LoginTask;
import org.digitalcampus.oppia.task.SendOTPTask;
import org.digitalcampus.oppia.task.result.EntityResult;
import org.digitalcampus.oppia.utils.UIUtils;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

public class LoginFragment extends AppFragment implements SubmitEntityListener<User> {

    @Inject
    ApiEndpoint apiEndpoint;

    //    changed by namratha
    private static final int PERMISSION_REQUEST_CODE = 101;
    private View inputLayout, otpLayout;
    private Spinner countrySpinner;
    private Spinner languageSpinner;
    private CountryCodePicker ccp;
    private EditText phoneEditText;
    private EditText otpDigit1, otpDigit2, otpDigit3, otpDigit4, otpDigit5, otpDigit6;
    private TextView otpTimer;
    private Button sendOtpBtn;
    private Button verifyOtpBtn;
    private Button registerBtn;
    private Button resendOtpBtnSms;
    private Button resendOtpBtnWhatsapp;
    private TextView otpSentTextView;
    private boolean isOtpTimerFinished = false;
    private CountDownTimer countDownTimer;
    private boolean shouldSendOtpAfterPermission = false;

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

        inputLayout = view.findViewById(R.id.layout_phone_entry);  // screen 1
        otpLayout = view.findViewById(R.id.layout_otp_entry);      // screen 2
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
        resendOtpBtnSms = view.findViewById(R.id.btn_resend_otp_sms);
        resendOtpBtnWhatsapp = view.findViewById(R.id.btn_resend_otp_whatsapp);
//        registerBtn = view.findViewById(R.id.action_register_btn);
        countrySpinner = view.findViewById(R.id.spinner_country);
        languageSpinner = view.findViewById(R.id.spinner_language);
        otpSentTextView = view.findViewById(R.id.text_otp_sent);
        ccp = view.findViewById(R.id.ccp);
        setupCountryAndLanguageSpinners();
        // By default show only screen 1
        otpLayout.setVisibility(View.GONE);
        verifyOtpBtn.setEnabled(false);
        sendOtpBtn.setEnabled(false);


        // Enable it when phone number is valid
        phoneEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                sendOtpBtn.setEnabled(s.length() >= 10);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        TextWatcher otpWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkOtpFieldsFilled();
            }
            @Override public void afterTextChanged(Editable s) {}
        };

        // Add to all 6 OTP fields
        otpDigit1.addTextChangedListener(otpWatcher);
        otpDigit2.addTextChangedListener(otpWatcher);
        otpDigit3.addTextChangedListener(otpWatcher);
        otpDigit4.addTextChangedListener(otpWatcher);
        otpDigit5.addTextChangedListener(otpWatcher);
        otpDigit6.addTextChangedListener(otpWatcher);

//        return binding.getRoot();
        return view;
    }

    private void checkOtpFieldsFilled() {
        boolean allFilled =
                !otpDigit1.getText().toString().trim().isEmpty() &&
                        !otpDigit2.getText().toString().trim().isEmpty() &&
                        !otpDigit3.getText().toString().trim().isEmpty() &&
                        !otpDigit4.getText().toString().trim().isEmpty() &&
                        !otpDigit5.getText().toString().trim().isEmpty() &&
                        !otpDigit6.getText().toString().trim().isEmpty();

        verifyOtpBtn.setEnabled(allFilled);
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
                sendOtpWithValidation();
            } else {
                // Set the flag so we know user intended to send OTP
                shouldSendOtpAfterPermission = true;
            }
        });

        resendOtpBtnSms.setOnClickListener( v ->{
            ccp.registerCarrierNumberEditText(phoneEditText);
            String fullNumber = ccp.getFormattedFullNumber();
//            if (isOtpTimerFinished) {
                sendOtpWithChannel(fullNumber,"sms");
//            }
        } );

        resendOtpBtnWhatsapp.setOnClickListener( v ->{
            ccp.registerCarrierNumberEditText(phoneEditText);
            String fullNumber = ccp.getFormattedFullNumber();
//            if (isOtpTimerFinished) {
                // Timer ended: ask for channel
                sendOtpWithChannel(fullNumber,"whatsapp");
//            }
        } );

        verifyOtpBtn.setOnClickListener(v -> verifyandloginOtp());



//        registerBtn.setOnClickListener(v -> {
//            WelcomeActivity wa = (WelcomeActivity) getActivity();
//            wa.switchTab(WelcomeActivity.TAB_REGISTER);
//        });
    }

    private void sendOtpWithValidation() {
        ccp.registerCarrierNumberEditText(phoneEditText);
        String mobile = phoneEditText.getText().toString().trim();
        if ((mobile.length() > 0) && !ccp.isValidFullNumber()) {
            UIUtils.showAlert(getActivity(), R.string.error, R.string.error_invalid_phone);
            return;
        }
        String fullNumber = ccp.getFormattedFullNumber();
        checkExternalProfileAndSendOtp(fullNumber);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) requireContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        return false;
    }

    private void checkExternalProfileAndSendOtp(String fullPhoneNumber) {
        if (!isNetworkAvailable()) {
            UIUtils.showAlert(getActivity(), R.string.error, R.string.error_connection_needed);
            return;
        }

        String url = apiEndpoint.getFullURL(requireContext(), Paths.EXTERNALPROFILE_PATH);

        ExternalProfileTask.execute(requireContext(), url, fullPhoneNumber, new ExternalProfileTask.ExternalProfileCallback() {
            @Override
            public void onSuccess(JSONObject fullResponse) {
                sendOtp();
            }

            @Override
            public void onNotFound() {
                UIUtils.showAlert(getActivity(), R.string.error, R.string.error_login);
            }

            @Override
            public void onError(String error) {
                UIUtils.showAlert(getActivity(), R.string.error, R.string.error_login);
            }
        });
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
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
                Toast.makeText(getActivity(), "Permissions granted.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), "Permissions denied. Cannot auto-fill OTP.", Toast.LENGTH_LONG).show();
            }

            //Call this in both cases — to allow OTP to be sent regardless
            sendOtpWithValidation();
            shouldSendOtpAfterPermission = false;
        }
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

        String fullNumber = ccp.getFormattedFullNumber();

        phoneEditText.setEnabled(false);
        sendOtpBtn.setEnabled(false);

        sendOtpWithChannel(fullNumber, "sms");
    }

    //    changed by namratha
    private void fetchChannels(String fullPhoneNumber) {
        if (!isNetworkAvailable()) {
            UIUtils.showAlert(getActivity(), R.string.error, R.string.error_connection_needed);
            return;
        }

        String url = apiEndpoint.getFullURL(requireContext(), Paths.CHANNEL_PATH);

        ChannelFetchTask.execute(requireContext(), url, fullPhoneNumber, new ChannelFetchTask.ChannelCallback() {
            @Override
            public void onChannelsFetched(boolean showSms, boolean showWhatsapp) {
                resendOtpBtnSms.setVisibility(showSms ? View.VISIBLE : View.GONE);
                resendOtpBtnSms.setEnabled(showSms);

                resendOtpBtnWhatsapp.setVisibility(showWhatsapp ? View.VISIBLE : View.GONE);
                resendOtpBtnWhatsapp.setEnabled(showWhatsapp);
            }

            @Override
            public void onError(String message) {
                Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    //    changed by namratha
    private void sendOtpWithChannel(String fullNumber, String channel) {
        if (!isNetworkAvailable()) {
            UIUtils.showAlert(getActivity(), R.string.error, R.string.error_connection_needed);
            return;
        }

        String url = apiEndpoint.getFullURL(requireContext(), Paths.SEND_OTP_PATH);

        SendOTPTask.execute(requireContext(), url, fullNumber, channel, new SendOTPTask.SendOtpCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(getActivity(), "OTP sent", Toast.LENGTH_SHORT).show();
                inputLayout.setVisibility(View.GONE);
                otpLayout.setVisibility(View.VISIBLE);
                String formattedMessage = "We have sent a one-time password (OTP) to \n" + fullNumber + " for verification";
                otpSentTextView.setText(formattedMessage);
                startOtpCountdown();
            }

            @Override
            public void onNotFound() {
                Toast.makeText(getActivity(), "Phone number not found.", Toast.LENGTH_SHORT).show();
                Toast.makeText(getActivity(), "Please contact your nearest Noora Health team member for Assistance.", Toast.LENGTH_SHORT).show();
                phoneEditText.setEnabled(true);
                sendOtpBtn.setEnabled(true);
            }

            @Override
            public void onError(String error) {
                Toast.makeText(getActivity(), "Phone number not found.", Toast.LENGTH_SHORT).show();
                Toast.makeText(getActivity(), "Please contact your nearest Noora Health team member for Assistance.", Toast.LENGTH_SHORT).show();
                phoneEditText.setEnabled(true);
                sendOtpBtn.setEnabled(true);
            }
        });
    }


    //    changed by namratha
    private void startOtpCountdown() {
        otpTimer.setVisibility(View.VISIBLE);
        phoneEditText.setEnabled(false);
        sendOtpBtn.setEnabled(false);
        isOtpTimerFinished = false;
        resendOtpBtnSms.setVisibility(View.GONE);
        resendOtpBtnWhatsapp.setVisibility(View.GONE);

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
                otpTimer.setVisibility(View.GONE);
                ccp.registerCarrierNumberEditText(phoneEditText);
                String fullNumber = ccp.getFormattedFullNumber();
                fetchChannels(fullNumber);
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

        if (!isNetworkAvailable()) {
            UIUtils.showAlert(getActivity(), R.string.error, R.string.error_connection_needed);
            return;
        }

        showProgressDialog(getString(R.string.login_process));

        User user = new User();
        user.setPhoneNo(ccp.getFormattedFullNumber());

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
        OtpSmsReceiver.setOtpListener(null); // avoid memory leaks
    }

    @Override
    public void onResume() {
        super.onResume();

        OtpSmsReceiver.setOtpListener(new OtpSmsReceiver.OtpListener() {
            @Override
            public void onOtpReceived(String otp) {
                autofillOtp(otp); // your function to set the digits
            }
        });
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
