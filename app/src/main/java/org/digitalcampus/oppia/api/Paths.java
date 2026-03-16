package org.digitalcampus.oppia.api;

public class Paths {

    // server path vars - new version
    public static final String OPPIAMOBILE_API = "api/v2/";
    public static final String LEADERBOARD_PATH = OPPIAMOBILE_API + "leaderboard/"; //done
    public static final String SERVER_AWARDS_PATH = OPPIAMOBILE_API + "awards/"; //done
    public static final String TRACKER_PATH = OPPIAMOBILE_API + "tracker/";
    public static final String SERVER_TAG_PATH = OPPIAMOBILE_API + "tag/"; //done
    public static final String SERVER_COURSES_PATH = OPPIAMOBILE_API + "course/"; //done
    public static final String COURSE_ACTIVITY_PATH = SERVER_COURSES_PATH + "%s/activity/"; //done
    public static final String COURSE_INFO_PATH = SERVER_COURSES_PATH + "%s"; //done
    public static final String QUIZ_SUBMIT_PATH = OPPIAMOBILE_API + "quizattempt/";
    public static final String RESET_PATH = OPPIAMOBILE_API + "reset/"; //not using
    public static final String REMEMBER_USERNAME_PATH = OPPIAMOBILE_API + "username/"; //not working properly for me
    public static final String REGISTER_PATH = OPPIAMOBILE_API + "register/"; //not used
    public static final String LOGIN_PATH = OPPIAMOBILE_API + "user/"; //done
    public static final String ACTIVITYLOG_PATH = "api/activitylog/";
    public static final String SERVER_INFO_PATH = "server/"; //done
    public static final String UPDATE_PROFILE_PATH = OPPIAMOBILE_API + "profileupdate/"; //update is prohibited
    public static final String DELETE_ACCOUNT_PATH = OPPIAMOBILE_API + "deleteaccount/";
    public static final String DOWNLOAD_ACCOUNT_DATA_PATH = OPPIAMOBILE_API + "downloaddata/"; //done
    public static final String CHANGE_PASSWORD_PATH = OPPIAMOBILE_API + "password/"; //password is not there now
    public static final String USER_COHORTS_PATH = OPPIAMOBILE_API + "cohorts/"; //done
    public static final String USER_PROFILE_PATH = OPPIAMOBILE_API + "profile/"; //done
    //    changed by namratha
    public static final String SEND_OTP_PATH = OPPIAMOBILE_API + "sendotp/"; //done
    public static final String CHANNEL_PATH = OPPIAMOBILE_API + "channel/"; //done
    public static final String EXTERNALPROFILE_PATH = OPPIAMOBILE_API + "externalprofile/"; //done

    private Paths() {
        throw new IllegalStateException("Utility class");
    }

}
