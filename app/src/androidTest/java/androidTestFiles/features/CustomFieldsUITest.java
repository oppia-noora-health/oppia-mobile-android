package androidTestFiles.features;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withHint;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withSpinnerText;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static androidTestFiles.utils.UITestActionsUtils.waitForView;
import static androidTestFiles.utils.ViewsUtils.onEditTextWithinTextInputLayoutWithId;

import android.Manifest;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.action.ViewActions;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

import org.digitalcampus.mobile.learning.BuildConfig;
import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.WelcomeActivity;
import org.digitalcampus.oppia.model.CustomField;
import org.digitalcampus.oppia.model.CustomFieldsRepository;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidTestFiles.utils.parent.MockedApiEndpointTest;

@RunWith(AndroidJUnit4.class)
public class CustomFieldsUITest extends MockedApiEndpointTest {
    @Rule
    public GrantPermissionRule mRuntimePermissionRule = GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE);

    @Mock
    protected CustomFieldsRepository customFieldsRepo;

    @Test
    public void checkFieldsAreShownAndValidated() throws  Exception {

        if (!BuildConfig.ALLOW_REGISTER_USER) {
            return;
        }

        List<CustomField> fields = new ArrayList<>();
        CustomField customString = new CustomField();
        customString.setType("str");
        customString.setKey("field01");
        customString.setHelperText("Helper text");
        customString.setLabel("String field");
        customString.setRequired(true);
        fields.add(customString);

        CustomField customInt = new CustomField();
        customInt.setType("int");
        customInt.setKey("field03");
        customInt.setHelperText("Custom int");
        customInt.setLabel("Custom int");
        fields.add(customInt);

        when(customFieldsRepo.getAll(any())).thenReturn(fields);

        try (ActivityScenario<WelcomeActivity> scenario = ActivityScenario.launch(WelcomeActivity.class)) {

            waitForView(withId(R.id.welcome_register))
                    .perform(scrollTo(), click());

            waitForView(withId(R.id.register_form_jobtitle_field)).perform(scrollTo());
            waitForView(withText("Helper text")).perform(scrollTo()).check(matches(isDisplayed()));
            waitForView(withText("Custom int")).perform(scrollTo()).check(matches(isDisplayed()));

            onEditTextWithinTextInputLayoutWithId(R.id.register_form_username_field)
                    .perform(closeSoftKeyboard(), scrollTo(), typeText("user"));

            onEditTextWithinTextInputLayoutWithId(R.id.register_form_email_field)
                    .perform(closeSoftKeyboard(), scrollTo(), typeText("email@email.com"));

            onEditTextWithinTextInputLayoutWithId(R.id.register_form_password_field)
                    .perform(closeSoftKeyboard(), scrollTo(), typeText("password"));

            onEditTextWithinTextInputLayoutWithId(R.id.register_form_password_again_field)
                    .perform(closeSoftKeyboard(), scrollTo(), typeText("password"));

            onEditTextWithinTextInputLayoutWithId(R.id.register_form_firstname_field)
                    .perform(closeSoftKeyboard(), scrollTo(), typeText("Name"));

            onEditTextWithinTextInputLayoutWithId(R.id.register_form_lastname_field)
                    .perform(closeSoftKeyboard(), scrollTo(), typeText("Surname"));

            waitForView(withId(R.id.register_btn)).perform(closeSoftKeyboard(), click());
            waitForView(withText(R.string.field_required)).perform(scrollTo()).check(matches(isDisplayed()));
        }
    }

    @Test
    public void checkFieldsDisplayedWithBooleanDependentField() throws  Exception {

        if (!BuildConfig.ALLOW_REGISTER_USER) {
            return;
        }

        List<CustomField> fields = new ArrayList<>();
        CustomField customSelect = new CustomField();
        customSelect.setType("bool");
        customSelect.setKey("field01");
        customSelect.setHelperText("Helper bool");
        customSelect.setLabel("Custom bool");
        fields.add(customSelect);

        CustomField customSelect2 = new CustomField();
        customSelect2.setType("str");
        customSelect2.setKey("field02");
        customSelect2.setFieldVisibleBy("field01");
        customSelect2.setHelperText("Dependant field");
        customSelect2.setLabel("Custom select 2");
        fields.add(customSelect2);

        when(customFieldsRepo.getAll(any())).thenReturn(fields);

        try (ActivityScenario<WelcomeActivity> scenario = ActivityScenario.launch(WelcomeActivity.class)) {

            waitForView(withId(R.id.welcome_register))
                    .perform(scrollTo(), click());

            waitForView(withId(R.id.register_form_jobtitle_field)).perform(scrollTo());

            waitForView(withHint("Custom bool")).perform(scrollTo(), click());

            // When we check the switch, the field is displayed
            waitForView(withText("Dependant field")).perform(ViewActions.scrollTo()).check(matches(isDisplayed()));

            waitForView(withHint(containsString("Custom bool"))).perform(scrollTo(), click());
        }
    }

    @Test
    public void checkFieldsDisplayedWithCollectionDependentField() throws  Exception {

        if (!BuildConfig.ALLOW_REGISTER_USER) {
            return;
        }

        List<CustomField> fields = new ArrayList<>();
        CustomField customSelect = new CustomField();
        customSelect.setType("choices");
        customSelect.setKey("field01");
        customSelect.setHelperText("Helper text");
        customSelect.setLabel("Custom select 1");
        customSelect.setRequired(true);
        customSelect.setCollectionName("test");
        customSelect.setCollection(Arrays.asList(
                new CustomField.CollectionItem("value01", "value 1"),
                new CustomField.CollectionItem("value02", "value 2")));
        fields.add(customSelect);

        CustomField customSelect2 = new CustomField();
        customSelect2.setType("str");
        customSelect2.setKey("field02");
        customSelect2.setFieldVisibleBy("field01");
        customSelect2.setValueVisibleBy("value01");
        customSelect2.setHelperText("Dependant field");
        customSelect2.setLabel("Custom select 2");
        fields.add(customSelect2);

        when(customFieldsRepo.getAll(any())).thenReturn(fields);

        try (ActivityScenario<WelcomeActivity> scenario = ActivityScenario.launch(WelcomeActivity.class)) {

            waitForView(withId(R.id.welcome_register))
                    .perform(scrollTo(), click());

            waitForView(withId(R.id.register_form_jobtitle_field)).perform(scrollTo());

            waitForView(withSpinnerText(containsString("Custom select 1"))).perform(scrollTo(), click());
            onData(anything()).atPosition(1).perform(click());

            // When we select the specific value, the field is displayed
            waitForView(withText("Dependant field")).perform(ViewActions.scrollTo()).check(matches(isDisplayed()));

            waitForView(withSpinnerText(containsString("value 1"))).perform(scrollTo(), click());
            onData(anything()).atPosition(1).perform(click());

            waitForView(withText("Dependant field")).check(matches(not(isDisplayed())));
        }
    }

}
