package testFiles.model;

import static org.junit.Assert.assertEquals;

import org.digitalcampus.oppia.model.CustomValue;
import org.junit.Test;

public class CustomValueModelTest {

    @Test
    public void constructorAndGetTest(){
        CustomValue cv1 = new CustomValue("my string");
        assertEquals("my string", cv1.getValue());
        assertEquals("my string", cv1.getValue().toString());

        CustomValue cv2 = new CustomValue(123);
        assertEquals(123, cv2.getValue());
        assertEquals("123", cv2.getValue().toString());

        CustomValue cv3 = new CustomValue(false);
        assertEquals(false, cv3.getValue());
        assertEquals("false", cv3.getValue().toString());

        CustomValue cv4 = new CustomValue((float) 123.123);
        assertEquals((float) 123.123, cv4.getValue());
        assertEquals("123.123", cv4.getValue().toString());
    }
}
