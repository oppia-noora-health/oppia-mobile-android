package androidTestFiles.features.xml;

import android.Manifest;
import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.digitalcampus.oppia.exception.InvalidXMLException;
import org.digitalcampus.oppia.model.CompleteCourse;
import org.digitalcampus.oppia.model.Media;
import org.digitalcampus.oppia.utils.storage.Storage;
import org.digitalcampus.oppia.utils.xmlreaders.CourseXMLReader;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.List;

import androidTestFiles.utils.FileUtils;
import androidx.test.rule.GrantPermissionRule;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static androidTestFiles.utils.parent.BaseTest.CORRECT_XML;
import static androidTestFiles.utils.parent.BaseTest.INCORRECT_XML;
import static androidTestFiles.utils.parent.BaseTest.PATH_COURSES_XML_TESTS;

@RunWith(AndroidJUnit4.class)
public class XMLReadersTest {
    @Rule
    public GrantPermissionRule mRuntimePermissionRule = GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE);

    private Context context;

    @Before
    public void setUp() throws Exception {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();

    }

    private String copyFile(String filename){
        String destination = Storage.getStorageLocationRoot(context) + File.separator + "test" + File.separator;
        FileUtils.copyFileFromAssets(context, PATH_COURSES_XML_TESTS, filename, new File(destination));
        return destination + filename;
    }

    @Test
    public void parseCourse_CompleteCorrect() throws Exception{

        String xmlLocation = copyFile(CORRECT_XML);
        CourseXMLReader reader = new CourseXMLReader(xmlLocation, -1, context);
        CompleteCourse course = reader.getParsedCourse();
        assertEquals(20150202182552d, course.getVersionId());
        List<Media> media = reader.getMedia();
        assertEquals(media.size(), 2);
    }

    @Test
    public void parseCourse_onlyMedia() throws Exception{

        String xmlLocation = copyFile(CORRECT_XML);
        CourseXMLReader reader = new CourseXMLReader(xmlLocation, -1, context);
        List<Media> media = reader.getMedia();
        assertEquals(media.size(), 2);
    }

    @Test
    public void parseCourse_nonExistingXML() throws Exception{
        try {
            CourseXMLReader reader = new CourseXMLReader("fakelocation.xml", -1, context);
            reader.getParsedCourse();
            Assert.fail("Should have thrown invalid XML exception");
        }
        catch (InvalidXMLException e){
            assertNotNull(e);
            assertTrue(e.getMessage().startsWith("Course XML not found"));
        }

    }

    @Test
    public void parseCompleteCourse_incorrectXML() throws Exception{

        String xmlLocation = copyFile(INCORRECT_XML);
        try {
            CourseXMLReader reader = new CourseXMLReader(xmlLocation, -1, context);
            reader.parse(CourseXMLReader.ParseMode.COMPLETE);
            reader.getParsedCourse();
            Assert.fail("Should have thrown invalid XML exception");
        }
        catch (InvalidXMLException e){
            assertNotNull(e);
        }

    }

}
