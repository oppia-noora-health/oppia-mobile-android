package androidTestFiles.model.coursecompletion;

import android.Manifest;
import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.digitalcampus.oppia.database.DbHelper;
import org.digitalcampus.oppia.exception.UserNotFoundException;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.User;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidTestFiles.database.sampledata.CourseData;
import androidTestFiles.database.sampledata.TrackerData;
import androidTestFiles.database.sampledata.UserData;
import androidx.test.rule.GrantPermissionRule;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class AllQuizzesPlusPercentTest {
    @Rule
    public GrantPermissionRule mRuntimePermissionRule = GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE);

    private Context context;

    @Before
    public void setUp() throws Exception {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        CourseData.loadData(context);
        UserData.loadData(context);
    }

    @Test
    public void testCourseCompleted() {
        DbHelper db = DbHelper.getInstance(context);
        User user = null;
        try {
            user = db.getUser(UserData.TEST_USER_1);
        } catch (UserNotFoundException e) {
            e.printStackTrace();
        }
        long courseId = db.getCourseIdByShortname(CourseData.TEST_COURSE_1, user.getUserId());
        Course course = db.getCourseWithProgress(courseId, user.getUserId());

        TrackerData.loadDataAllQuizzesPlusPercentCourseComplete(context);

        boolean result = course.isComplete(context, user, Course.COURSE_COMPLETE_ALL_QUIZZES_PLUS_PERCENT, 50);

        assertTrue(result);

    }

    @Test
    public void testCourseNotCompleted() {
        DbHelper db = DbHelper.getInstance(context);
        User user = null;
        try {
            user = db.getUser(UserData.TEST_USER_1);
        } catch (UserNotFoundException e) {
            e.printStackTrace();
        }
        long courseId = db.getCourseIdByShortname(CourseData.TEST_COURSE_1, user.getUserId());
        Course course = db.getCourseWithProgress(courseId, user.getUserId());

        TrackerData.loadDataAllQuizzesPlusPercentCourseNotComplete(context);

        boolean result = course.isComplete(context, user, Course.COURSE_COMPLETE_ALL_QUIZZES_PLUS_PERCENT, 80);

        assertFalse(result);
    }
}
