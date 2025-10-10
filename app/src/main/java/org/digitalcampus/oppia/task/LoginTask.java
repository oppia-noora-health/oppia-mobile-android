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

package org.digitalcampus.oppia.task;

import static org.digitalcampus.oppia.holder.ActivityHolder.getActivity;

import android.content.Context;
import android.util.Log;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.analytics.Analytics;
import org.digitalcampus.oppia.api.ApiEndpoint;
import org.digitalcampus.oppia.api.Paths;
import org.digitalcampus.oppia.application.SessionManager;
import org.digitalcampus.oppia.database.DbHelper;
import org.digitalcampus.oppia.exception.UserNotFoundException;
import org.digitalcampus.oppia.listener.SubmitEntityListener;
import org.digitalcampus.oppia.model.User;
import org.digitalcampus.oppia.task.result.EntityResult;
import org.digitalcampus.oppia.utils.ConnectionUtils;
import org.digitalcampus.oppia.utils.HTTPClientUtils;
import org.digitalcampus.oppia.utils.MetaDataUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginTask extends APIRequestTask<User, Object, EntityResult<User>> {

    private SubmitEntityListener mStateListener;
    //    changed by namratha
    private String otpCode;
    private String country;
    private String language;

    public LoginTask(Context ctx, ApiEndpoint api) {
        super(ctx, api);
    }

    @Override
    protected EntityResult<User> doInBackground(User... params) {

        User user = params[0];
        EntityResult<User> result = new EntityResult<>();
        result.setEntity(user);

        if (ConnectionUtils.isNetworkConnected(ctx)) {
            loginRemotely(user, result);
        }
//        changed by namratha
//        else{
//            // Only try to login locally if no connection available
//            loginLocalOfflineUser(user, result);
//        }

        return result;
    }
    //    changed by namratha
    public void setOtpCode(String otpCode) {
        this.otpCode = otpCode;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    private void loginLocalOfflineUser(User user, EntityResult<User> result){
        try {
            User localUser = DbHelper.getInstance(ctx).getUser(user.getUsername());
            Log.d(TAG, "logged pw: " + localUser.getPasswordEncrypted());
            Log.d(TAG, "entered pw: " + user.getPasswordEncrypted());

            if (SessionManager.isUserApiKeyValid(user.getUsername()) &&
                    localUser.getPasswordEncrypted().equals(user.getPasswordEncrypted())) {
                result.setSuccess(true);
                result.setResultMessage(ctx.getString(R.string.login_complete));
                result.getEntity().setLocalUser(true);
            } else {
                result.setSuccess(false);
                result.setResultMessage(ctx.getString(R.string.offline_user_invalid_password));
            }
        } catch (UserNotFoundException unfe) {
            result.setSuccess(false);
            result.setResultMessage(ctx.getString(R.string.offline_user_not_found));
        }
    }

//    private void loginRemotely(User user, EntityResult<User> result){
//
//        User localUser;
//        try {
//            localUser = DbHelper.getInstance(ctx).getUser(user.getUsername());
//        }
//        catch (UserNotFoundException unfe){
//            localUser = null;
//        }
//
//        try {
//            // update progress dialog
//            publishProgress(ctx.getString(R.string.login_process));
//            JSONObject json = new JSONObject();
//            json.put("username", user.getUsername());
//            json.put("password", user.getPassword());
//
//            OkHttpClient client = HTTPClientUtils.getClient(ctx);
//            Request request = new Request.Builder()
//                    .url(apiEndpoint.getFullURL(ctx, Paths.LOGIN_PATH))
//                    .post(RequestBody.create(json.toString(), HTTPClientUtils.MEDIA_TYPE_JSON))
//                    .build();
//
//            Response response = client.newCall(request).execute();
//            if (response.isSuccessful()) {
//                JSONObject jsonResp = new JSONObject(response.body().string());
//                user.updateFromJSON(ctx, jsonResp);
//                DbHelper.getInstance(ctx).addOrUpdateUser(user);
//                new MetaDataUtils(ctx).saveMetaData(jsonResp);
//                result.setSuccess(true);
//                result.setResultMessage(ctx.getString(R.string.login_complete));
//
//                if (localUser != null &&
//                        localUser.getPasswordEncrypted().equals(user.getPasswordEncrypted())) {
//                    user.setLocalUser(true);
//                }
//
//            } else {
//                if (response.code() == 400 || response.code() == 401) {
//                    result.setSuccess(false);
//                    result.setResultMessage(ctx.getString(R.string.error_login));
//                } else {
//                    result.setSuccess(false);
//                    result.setResultMessage(ctx.getString(R.string.error_connection));
//                }
//            }
//
//        } catch (javax.net.ssl.SSLHandshakeException e) {
//            Log.d(TAG, "SSLHandshakeException: ", e);
//            result.setSuccess(false);
//            result.setResultMessage(ctx.getString(R.string.error_connection_ssl));
//        } catch (UnsupportedEncodingException e) {
//            result.setSuccess(false);
//            result.setResultMessage(ctx.getString(R.string.error_connection));
//        } catch (IOException e) {
//            result.setSuccess(false);
//            result.setResultMessage(ctx.getString(R.string.error_connection_required));
//        } catch (JSONException e) {
//            Analytics.logException(e);
//            Log.d(TAG, "JSONException: ", e);
//            result.setSuccess(false);
//            result.setResultMessage(ctx.getString(R.string.error_processing_response));
//        }
//
//    }

    private void loginRemotely(User user, EntityResult<User> result) {
        try {
            publishProgress(ctx.getString(R.string.login_process));
            JSONObject json = new JSONObject();
            json.put("phone_number", user.getPhoneNo());
            json.put("code", otpCode); // OTP value

            OkHttpClient client = HTTPClientUtils.getClient(ctx);
            Request request = new Request.Builder()
                    .url(apiEndpoint.getFullURL(ctx, Paths.LOGIN_PATH))
                    .post(RequestBody.create(json.toString(), HTTPClientUtils.MEDIA_TYPE_JSON))
                    .build();

            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                JSONObject jsonResp = new JSONObject(response.body().string());

                // Create a latch to wait for the async call to finish
                java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);

                // Fetch external profile
                String profileUrl = apiEndpoint.getFullURL(ctx, Paths.EXTERNALPROFILE_PATH);
                String fullPhoneNumber = user.getPhoneNo();

                ExternalProfileTask.execute(ctx, profileUrl, fullPhoneNumber, country , language, new ExternalProfileTask.ExternalProfileCallback() {
                    @Override
                    public void onSuccess(JSONObject fullResponse) {
                        try {
                            Log.d("ExternalProfile", "Fetched profile: " + fullResponse.toString());

                            String username = fullResponse.optString("username", null);
                            if (username != null) {
                                user.setUsername(username);
                            }

                            user.updateFromJSON(ctx, fullResponse);
                            DbHelper.getInstance(ctx).addOrUpdateUser(user);
                            new MetaDataUtils(ctx).saveMetaData(fullResponse);

                            result.setSuccess(true);
                            result.setResultMessage(ctx.getString(R.string.login_complete));
                        } catch (Exception e) {
                            Log.e("ExternalProfile", "Error updating user: ", e);
                            result.setSuccess(false);
                            result.setResultMessage("Error processing profile data");
                        } finally {
                            latch.countDown();
                        }
                    }

                    @Override
                    public void onNotFound() {
                        Log.w("ExternalProfile", "Profile not found after login.");
                        result.setSuccess(false);
                        result.setResultMessage("Profile not found");
                        latch.countDown();
                    }

                    @Override
                    public void onError(String error) {
                        Log.e("ExternalProfile", "Error fetching profile: " + error);
                        result.setSuccess(false);
                        result.setResultMessage("Error fetching profile: " + error);
                        latch.countDown();
                    }
                });

                // Wait for external profile task to finish
                latch.await();  // This blocks the thread until countDown() is called

            } else {
                if (response.code() == 400 || response.code() == 401) {
                    result.setSuccess(false);
                    result.setResultMessage(ctx.getString(R.string.error_invalid_otp));
                } else {
                    result.setSuccess(false);
                    result.setResultMessage(ctx.getString(R.string.error_connection));
                }
            }

        } catch (javax.net.ssl.SSLHandshakeException e) {
            Log.d(TAG, "SSLHandshakeException: ", e);
            result.setSuccess(false);
            result.setResultMessage(ctx.getString(R.string.error_connection_ssl));
        } catch (IOException | JSONException | InterruptedException e) {
            Log.e(TAG, "Login error: ", e);
            result.setSuccess(false);
            result.setResultMessage(ctx.getString(R.string.error_processing_response));
        }
    }

    @Override
    protected void onPostExecute(EntityResult<User> result) {
        synchronized (this) {
            if (mStateListener != null) {
                mStateListener.submitComplete(result);
            }
        }
    }

    public void setLoginListener(SubmitEntityListener srl) {
        synchronized (this) {
            mStateListener = srl;
        }
    }

}
