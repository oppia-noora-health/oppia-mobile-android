package org.digitalcampus.oppia.model;

import android.content.Context;

import org.digitalcampus.oppia.database.DbHelper;

public class TrackerLogRepository {


    public String getLastTrackerDatetime(Context context) throws Exception {
        DbHelper db = DbHelper.getInstance(context);
//        User user = db.getUser(SessionManager.getUsername(context)); #reminders-multi-user
        return db.getLastTrackerDatetime(-1/*user.getUserId()*/);
    }
}
