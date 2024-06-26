package androidTestFiles.features.course;

import android.Manifest;
import android.content.Context;
import android.util.Log;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.GrantPermissionRule;

import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.application.SessionManager;
import org.digitalcampus.oppia.listener.InstallCourseListener;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.DownloadProgress;
import org.digitalcampus.oppia.task.DeleteCourseTask;
import org.digitalcampus.oppia.task.InstallDownloadedCoursesTask;
import org.digitalcampus.oppia.task.result.BasicResult;
import org.digitalcampus.oppia.utils.storage.Storage;
import org.digitalcampus.oppia.utils.storage.StorageAccessStrategy;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;

import androidTestFiles.utils.CourseUtils;
import androidTestFiles.utils.FileUtils;
import androidTestFiles.database.BaseTestDB;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static androidTestFiles.utils.parent.BaseTest.CORRECT_COURSE;

@RunWith(Parameterized.class)
public class DeleteCourseTest extends BaseTestDB {
    public static final String TAG = DeleteCourseTest.class.getSimpleName();
    @Rule
    public GrantPermissionRule mRuntimePermissionRule = GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE);

    private Context context;
    private StorageAccessStrategy storageStrategy;


    public DeleteCourseTest(StorageAccessStrategy storageStrategy) {
        this.storageStrategy = storageStrategy;
    }

    @Parameterized.Parameters
    public static StorageAccessStrategy[] storageStrategies() {
        return FileUtils.getStorageStrategiesBasedOnDeviceAvailableStorage();
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();

        setStorageStrategy();
    }


    //Run test once for every StorageStrategy (Internal, External)
    public void setStorageStrategy() {

        Log.v(TAG, "Using Strategy: " + storageStrategy.getStorageType());
        Storage.setStorageStrategy(storageStrategy);

        when(prefs.getString(eq(PrefsActivity.PREF_STORAGE_OPTION), anyString())).thenReturn(storageStrategy.getStorageType());

    }

    @Test
    public void deleteCourse_success() throws Exception {

        if (!Storage.getStorageStrategy().isStorageAvailable(context)) {
            return;
        }

        CourseUtils.cleanUp();

        installTestCourse();

        File modulesPath = new File(Storage.getCoursesPath(context));
        assertTrue(modulesPath.exists());
        String[] children = modulesPath.list();
        assertEquals(1, children.length);  //Check that the course exists in the "modules" directory

        long userId = getDbHelper().getUserId(SessionManager.getUsername(context));
        String shortName = children.length != 0 ? children[0].toLowerCase(Locale.US) : "";
        long courseId = getDbHelper().getCourseID(shortName);
        Course c = getDbHelper().getCourseWithProgress(courseId, userId);
        assertNotNull(c);   //Check that the course exists in the database

        deleteTestCourse(c, context);

//        assertTrue(response.isResult());

        c = getDbHelper().getCourseWithProgress(courseId, userId);
        assertNull(c);   //Check that the course does not exists in the database

        File finalPath = new File(modulesPath, children[0]);
        assertFalse(finalPath.exists());

        assertEquals(0, modulesPath.list().length);    //Check that the course does not exists in the "modules" directory

    }

    @Test
    public void deleteCourse_nonExistingCourse() throws Exception {

        CourseUtils.cleanUp();

        File modulesPath = new File(Storage.getCoursesPath(InstrumentationRegistry.getInstrumentation().getTargetContext()));
        assertTrue(modulesPath.exists());
        String[] children = modulesPath.list();
        assertEquals(0, children.length); //Check that the course does not exists in the "modules" directory

        long userId = getDbHelper().getUserId(SessionManager.getUsername(context));
        String shortName = children.length != 0 ? children[0].toLowerCase(Locale.US) : "";
        long courseId = getDbHelper().getCourseID(shortName);
        Course c = getDbHelper().getCourseWithProgress(courseId, userId);
        assertNull(c);   //Check that the course does not exists in the database

        deleteTestCourse(c, context);

        c = getDbHelper().getCourseWithProgress(courseId, userId);
        assertNull(c);   //Check that the course does not exists in the database

        assertEquals(0, modulesPath.list().length);    //Check that the course does not exists in the "modules" directory
    }

    @Test
    //Install a course that is already in the database but not in the storage system
    public void deleteCourse_courseAlreadyOnDatabase() throws Exception {
        CourseUtils.cleanUp();

        installTestCourse();

        File modulesPath = new File(Storage.getCoursesPath(InstrumentationRegistry.getInstrumentation().getTargetContext()));
        assertTrue(modulesPath.exists());
        String[] children = modulesPath.list();
        assertEquals(1, children.length);  //Check that the course exists in the "modules" directory

        long userId = getDbHelper().getUserId(SessionManager.getUsername(context));
        String shortName = children.length != 0 ? children[0].toLowerCase(Locale.US) : "";
        long courseId = getDbHelper().getCourseID(shortName);
        Course c = getDbHelper().getCourseWithProgress(courseId, userId);
        assertNotNull(c);   //Check that the course exists in the database

        deleteTestCourse(c, context);

        c = getDbHelper().getCourseWithProgress(courseId, userId);
        assertNull(c);   //Check that the course does not exists in the database


        File finalPath = new File(modulesPath, children[0]);
        assertFalse(finalPath.exists());

        assertEquals(0, modulesPath.list().length);    //Check that the course does not exists in the "modules" directory


    }

    private void installTestCourse() {
        //Proceed with the installation of the course

        final CountDownLatch signal = new CountDownLatch(1);  //Control AsyncTask sincronization for testing

        String filename = CORRECT_COURSE;
        CourseUtils.cleanUp();

        FileUtils.copyZipFromAssets(context, filename);  //Copy course zip from assets to download path

        InstallDownloadedCoursesTask imTask = new InstallDownloadedCoursesTask(context);
        imTask.setInstallerListener(new InstallCourseListener() {
            @Override
            public void installComplete(BasicResult result) {
                signal.countDown();
            }

            @Override
            public void installProgressUpdate(DownloadProgress dp) {
            }
        });
        imTask.execute();

        try {
            signal.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public static void deleteTestCourse(Course course, Context context) {

        final CountDownLatch signal = new CountDownLatch(1);  //Control AsyncTask sincronization for testing

        DeleteCourseTask task = new DeleteCourseTask(context);
        task.setOnDeleteCourseListener(r -> signal.countDown());
        task.execute(course);

        try {
            signal.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
