package org.digitalcampus.oppia.holder;

import org.digitalcampus.oppia.model.Course;

public class CourseHolder {

    private static Course course;

    public static void setCourse(Course c) {
        course = c;
    }

    public static Course getCourse() {
        return course;
    }

    public static void clear() {
        course = null;
    }
}
