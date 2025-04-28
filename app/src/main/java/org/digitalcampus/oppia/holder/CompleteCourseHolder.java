package org.digitalcampus.oppia.holder;

import org.digitalcampus.oppia.model.CompleteCourse;

public class CompleteCourseHolder {

    private static CompleteCourse completeCourse;

    public static void setCompleteCourse(CompleteCourse course) {
        completeCourse = course;
    }

    public static CompleteCourse getCompleteCourse() {
        return completeCourse;
    }

    public static void clear() {
        completeCourse = null;
    }
}
