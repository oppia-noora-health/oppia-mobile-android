package org.digitalcampus.oppia.holder;

import org.digitalcampus.oppia.model.Activity;

public class ActivityHolder {
    private static Activity activity;

    public static void setActivity(Activity act) {
        activity = act;
    }

    public static Activity getActivity() {
        return activity;
    }

    public static void clear() {
        activity = null;
    }
}
