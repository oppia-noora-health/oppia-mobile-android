package androidTestFiles.features.course;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.model.CourseInstallViewAdapter;
import org.digitalcampus.oppia.task.CourseInfoTask;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;

import androidTestFiles.utils.FileUtils;
import androidTestFiles.utils.MockApiEndpoint;
import androidTestFiles.utils.parent.BaseTest;
import androidTestFiles.utils.parent.MockedApiEndpointTaskTest;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;

public class CourseInfoTaskTest  extends MockedApiEndpointTaskTest {

    private static final String NOT_JSON_RESPONSE = BaseTest.PATH_RESPONSES + "/response_body_error_message.txt";
    private static final String VALID_COURSEINFO_RESPONSE = BaseTest.PATH_RESPONSES + "/course/response_200_course_info.json";

    private CourseInstallViewAdapter courseResult;
    private String errorMsg;

    private Context context;

    @Before
    public void setUp() throws Exception {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
    }

    @After
    public void tearDown() throws Exception {
        if (mockServer!=null)
            mockServer.shutdown();
    }


    private void fetchCourseInfoSync(){
        final CountDownLatch signal = new CountDownLatch(1);  //Control AsyncTask sincronization for testing

        CourseInfoTask task = new CourseInfoTask(context, new MockApiEndpoint(mockServer));
        task.setListener(new CourseInfoTask.CourseInfoListener() {
            @Override
            public void onSuccess(CourseInstallViewAdapter course) {
                courseResult = course;
                signal.countDown();
            }
            @Override
            public void onError(String error) {
                errorMsg = error;
                signal.countDown();
            }
        });

        task.execute("test");

        try {
            signal.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void fetchCourseInfo_validCourse() throws Exception{

        startServer(200, FileUtils.getStringFromFile(
                InstrumentationRegistry.getInstrumentation().getContext(), VALID_COURSEINFO_RESPONSE));
        fetchCourseInfoSync();

        assertNull(errorMsg);
        assertEquals(courseResult.getTitle("en"), "Test course");
        assertEquals(courseResult.getDisplayAuthorName(), "Alex Little");
    }

    @Test
    public void fetchCourseInfo_malformedResponse() throws Exception{
        startServer(200, NOT_JSON_RESPONSE);
        fetchCourseInfoSync();
        assertNull(courseResult);
        assertEquals(errorMsg, context.getString(R.string.error_processing_response));
    }

    @Test
    public void fetchCourseInfo_notFoundCourse() {
        startServer(404, "");
        fetchCourseInfoSync();
        assertNull(courseResult);
        assertEquals(errorMsg, context.getString(R.string.open_digest_errors_course_not_found));
    }

    @Test
    public void fetchCourseInfo_badRequest() {
        startServer(500, "");
        fetchCourseInfoSync();
        assertNull(courseResult);
        assertEquals(errorMsg, context.getString(R.string.error_connection));
    }
}
