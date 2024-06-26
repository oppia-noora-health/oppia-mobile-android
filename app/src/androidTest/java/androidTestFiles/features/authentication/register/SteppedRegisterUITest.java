package androidTestFiles.features.authentication.register;

import static androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasSibling;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static androidTestFiles.utils.UITestActionsUtils.waitForView;
import static androidTestFiles.utils.ViewsUtils.onEditTextWithinTextInputLayout;
import static androidTestFiles.utils.ViewsUtils.onEditTextWithinTextInputLayoutWithId;
import static androidTestFiles.utils.ViewsUtils.onErrorViewWithinTextInputLayoutWithId;
import static androidTestFiles.utils.ViewsUtils.withHintInInputLayout;
import static androidTestFiles.utils.parent.BaseTest.PATH_CUSTOM_FIELDS_TESTS;

import android.Manifest;
import android.widget.Spinner;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.uiautomator.UiDevice;

import org.digitalcampus.mobile.learning.BuildConfig;
import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.WelcomeActivity;
import org.digitalcampus.oppia.database.DbHelper;
import org.digitalcampus.oppia.model.CustomField;
import org.digitalcampus.oppia.model.CustomFieldsRepository;
import org.digitalcampus.oppia.utils.ui.fields.ValidableTextInputLayout;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import java.util.List;

import androidTestFiles.utils.FileUtils;
import androidTestFiles.utils.matchers.SpinnerMatcher;
import androidTestFiles.utils.parent.MockedApiEndpointTest;

@RunWith(AndroidJUnit4.class)
public class SteppedRegisterUITest extends MockedApiEndpointTest {

    @Rule
    public GrantPermissionRule mRuntimePermissionRule = GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE);

    private static final String BASIC_DEPENDANT_STEPS = PATH_CUSTOM_FIELDS_TESTS + "/dependant_fields.json";
    private static final String REGISTER_STEPS_NORMAL = PATH_CUSTOM_FIELDS_TESTS + "/custom_fields.json";
    private static final String ADVANCED_SCENARIOS = PATH_CUSTOM_FIELDS_TESTS + "/advanced_scenarios.json";

    @Mock
    protected CustomFieldsRepository customFieldsRepo;
    private ActivityScenario<WelcomeActivity> scenario;

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void cleanup() {
        try {
            scenario.close();
        } catch (Exception e) {
            // ignore
        }
    }

    private void setRegisterSteps(String JsonDeclarationPath) throws Exception {
        String fieldsData = FileUtils.getStringFromFile(
                InstrumentationRegistry.getInstrumentation().getContext(), JsonDeclarationPath);
        CustomField.loadCustomFields(InstrumentationRegistry.getInstrumentation().getContext(), fieldsData);
        List<CustomField.RegisterFormStep> steps = CustomField.parseRegisterSteps(fieldsData);
        DbHelper db = DbHelper.getInstance(InstrumentationRegistry.getInstrumentation().getContext());
        List<CustomField> profileCustomFields = db.getCustomFields();
        when(customFieldsRepo.getRegisterSteps(any())).thenReturn(steps);
        when(customFieldsRepo.getAll(any())).thenReturn(profileCustomFields);
    }


    @Test
    public void showsStepperIfSteppedRegistration() throws Exception {

        if (!BuildConfig.ALLOW_REGISTER_USER) {
            return;
        }

        setRegisterSteps(REGISTER_STEPS_NORMAL);

        try (ActivityScenario<WelcomeActivity> scenario = ActivityScenario.launch(WelcomeActivity.class)) {
            waitForView(withId(R.id.welcome_register)).perform(scrollTo(), click());

            waitForView(withId(R.id.frame_stepper_indicator)).check(matches(isDisplayed()));
            waitForView(withText("First step")).check(matches(isDisplayed()));
        }

    }

    @Test
    public void dontAdvanceStepIfFieldErrors() throws Exception {

        if (!BuildConfig.ALLOW_REGISTER_USER) {
            return;
        }

        setRegisterSteps(REGISTER_STEPS_NORMAL);

        try (ActivityScenario<WelcomeActivity> scenario = ActivityScenario.launch(WelcomeActivity.class)) {
            waitForView(withId(R.id.welcome_register)).perform(scrollTo(), click());

            onEditTextWithinTextInputLayoutWithId(R.id.register_form_username_field)
                    .perform(closeSoftKeyboard(), scrollTo(), replaceText(""));

            waitForView(withId(R.id.next_btn)).perform(click());

            // Check we're still in the first step
            waitForView(withText("First step")).check(matches(isDisplayed()));

            onErrorViewWithinTextInputLayoutWithId(R.id.register_form_username_field)
                    .check(matches(withText(R.string.field_required)));
        }
    }

    @Test
    public void advanceNextStepIfAllFieldsCorrect() throws Exception {

        if (!BuildConfig.ALLOW_REGISTER_USER) {
            return;
        }

        setRegisterSteps(REGISTER_STEPS_NORMAL);

        try (ActivityScenario<WelcomeActivity> scenario = ActivityScenario.launch(WelcomeActivity.class)) {

            waitForView(withId(R.id.welcome_register)).perform(scrollTo(), click());

            onEditTextWithinTextInputLayoutWithId(R.id.register_form_username_field)
                    .perform(closeSoftKeyboard(), scrollTo(), typeText("Username"));
            onEditTextWithinTextInputLayoutWithId(R.id.register_form_password_field)
                    .perform(closeSoftKeyboard(), scrollTo(), typeText("password1"));
            onEditTextWithinTextInputLayoutWithId(R.id.register_form_password_again_field)
                    .perform(closeSoftKeyboard(), scrollTo(), typeText("password1"));

            waitForView(withId(R.id.next_btn)).perform(closeSoftKeyboard(), click());

            waitForView(withText("Second step")).check(matches(isDisplayed()));

        }
    }


    @Test
    public void keepValuesGoingStepsBack() throws Exception {

        if (!BuildConfig.ALLOW_REGISTER_USER) {
            return;
        }

        setRegisterSteps(REGISTER_STEPS_NORMAL);

        try (ActivityScenario<WelcomeActivity> scenario = ActivityScenario.launch(WelcomeActivity.class)) {
            waitForView(withId(R.id.welcome_register)).perform(scrollTo(), click());

            onEditTextWithinTextInputLayoutWithId(R.id.register_form_username_field)
                    .perform(closeSoftKeyboard(), scrollTo(), typeText("Username"));
            onEditTextWithinTextInputLayoutWithId(R.id.register_form_password_field)
                    .perform(closeSoftKeyboard(), scrollTo(), typeText("password1"));
            onEditTextWithinTextInputLayoutWithId(R.id.register_form_password_again_field)
                    .perform(closeSoftKeyboard(), scrollTo(), typeText("password1"));

            waitForView(withId(R.id.next_btn)).perform(closeSoftKeyboard(), click());
            waitForView(withText("Second step")).check(matches(isDisplayed()));
            waitForView(withId(R.id.prev_btn)).perform(click());
            onEditTextWithinTextInputLayoutWithId(R.id.register_form_username_field)
                    .check(matches(withText("Username")));
        }
    }

    @Test
    public void showDependantFieldOnSameStep() throws Exception {

        if (!BuildConfig.ALLOW_REGISTER_USER) {
            return;
        }

        setRegisterSteps(BASIC_DEPENDANT_STEPS);

        try (ActivityScenario<WelcomeActivity> scenario = ActivityScenario.launch(WelcomeActivity.class)) {
            waitForView(withId(R.id.welcome_register)).perform(scrollTo(), click());

            waitForView(allOf(instanceOf(Spinner.class), hasSibling(withText(startsWith("Position")))))
                    .perform(scrollTo())
                    .check(matches(isDisplayed()))
                    .perform(click());

            waitForView(withText("Other")).perform(click());
            waitForView(allOf(instanceOf(ValidableTextInputLayout.class),
                    withHintInInputLayout(startsWith("Please specify"))))
                    .check(matches(isDisplayed()));
        }
    }

    @Test
    public void UpdateDependantCollection() throws Exception {

        if (!BuildConfig.ALLOW_REGISTER_USER) {
            return;
        }

        setRegisterSteps(ADVANCED_SCENARIOS);

        try (ActivityScenario<WelcomeActivity> scenario = ActivityScenario.launch(WelcomeActivity.class)) {
            waitForView(withId(R.id.welcome_register)).perform(scrollTo(), click());

            waitForView(allOf(instanceOf(Spinner.class), SpinnerMatcher.withSpinnerSelectedItemText("Select county")))
                    .perform(scrollTo()).perform(click());
            waitForView(withText("Area1")).perform(click());

            waitForView(allOf(instanceOf(Spinner.class), SpinnerMatcher.withSpinnerSelectedItemText("Select district")))
                    .perform(scrollTo(), click());
            waitForView(withText("region1")).perform(click());

            waitForView(allOf(instanceOf(Spinner.class), SpinnerMatcher.withSpinnerSelectedItemText("Area1")))
                    .perform(scrollTo()).perform(click());
            waitForView(withText("Area2")).perform(click());

            waitForView(allOf(instanceOf(Spinner.class), SpinnerMatcher.withSpinnerSelectedItemText("Select district")))
                    .perform(scrollTo())
                    .check(matches(isDisplayed()))
                    .perform(click());
            waitForView(withText("region3"))
                    .check(matches(isDisplayed()))
                    .perform(click());
        }
    }


    @Test
    public void ShowDependantFieldOnDifferentStep() throws Exception {

        if (!BuildConfig.ALLOW_REGISTER_USER) {
            return;
        }

        setRegisterSteps(ADVANCED_SCENARIOS);

        try (ActivityScenario<WelcomeActivity> scenario = ActivityScenario.launch(WelcomeActivity.class)) {
            waitForView(withId(R.id.welcome_register)).perform(scrollTo(), click());

            waitForView(allOf(instanceOf(Spinner.class), hasSibling(withText(startsWith("Select county")))))
                    .perform(scrollTo()).perform(click());
            waitForView(withText("Area1")).perform(click());

            waitForView(withId(R.id.next_btn)).perform(click());

            waitForView(withText("Area1 dependant"))
                    .check(matches(isDisplayed()));

            //Now we go back and select a different value to check that it is not displayed
            waitForView(withId(R.id.prev_btn)).perform(click());

            waitForView(allOf(instanceOf(Spinner.class), hasSibling(withText(startsWith("Select county")))))
                    .perform(scrollTo()).perform(click());
            waitForView(withText("Area2")).perform(click());

            waitForView(withText("Area1 dependant"))
                    .check(matches(not(isDisplayed())));
        }
    }

    @Test
    public void showDependantStepWithValue() throws Exception {

        if (!BuildConfig.ALLOW_REGISTER_USER) {
            return;
        }

        setRegisterSteps(BASIC_DEPENDANT_STEPS);

        try (ActivityScenario<WelcomeActivity> scenario = ActivityScenario.launch(WelcomeActivity.class)) {
            waitForView(withId(R.id.welcome_register)).perform(scrollTo(), click());

            waitForView(allOf(instanceOf(Spinner.class), hasSibling(withText(startsWith("Position")))))
                    .perform(scrollTo())
                    .check(matches(isDisplayed()))
                    .perform(click());

            waitForView(withText("Other")).perform(click());
            onEditTextWithinTextInputLayout(allOf(instanceOf(ValidableTextInputLayout.class),
                    withHintInInputLayout(startsWith("Please specify"))))
                    .perform(closeSoftKeyboard(), scrollTo(), typeText("None"));

            waitForView(withId(R.id.next_btn)).perform(closeSoftKeyboard(), click());

            waitForView(withText("Other role info")).check(matches(isDisplayed()));
        }
    }

    @Test
    public void showDependantStepWithNegatedValue() throws Exception {

        if (!BuildConfig.ALLOW_REGISTER_USER) {
            return;
        }

        setRegisterSteps(BASIC_DEPENDANT_STEPS);

        try (ActivityScenario<WelcomeActivity> scenario = ActivityScenario.launch(WelcomeActivity.class)) {
            waitForView(withId(R.id.welcome_register)).perform(scrollTo(), click());

            waitForView(allOf(instanceOf(Spinner.class), hasSibling(withText(startsWith("Position")))))
                    .perform(scrollTo())
                    .check(matches(isDisplayed()))
                    .perform(click());

            waitForView(withText("Role2")).perform(click());

            waitForView(withId(R.id.next_btn)).perform(click());

            waitForView(withText("Personal info")).check(matches(isDisplayed()));
        }
    }

    @Test
    public void ShowRegisterButtonOnLastStep() throws Exception {

        if (!BuildConfig.ALLOW_REGISTER_USER) {
            return;
        }

        setRegisterSteps(REGISTER_STEPS_NORMAL);

        try (ActivityScenario<WelcomeActivity> scenario = ActivityScenario.launch(WelcomeActivity.class)) {
            waitForView(withId(R.id.welcome_register)).perform(scrollTo(), click());

            onEditTextWithinTextInputLayoutWithId(R.id.register_form_username_field)
                    .perform(closeSoftKeyboard(), scrollTo(), typeText("Username"));
            onEditTextWithinTextInputLayoutWithId(R.id.register_form_password_field)
                    .perform(closeSoftKeyboard(), scrollTo(), typeText("password1"));
            onEditTextWithinTextInputLayoutWithId(R.id.register_form_password_again_field)
                    .perform(closeSoftKeyboard(), scrollTo(), typeText("password1"));

            waitForView(withId(R.id.next_btn)).perform(closeSoftKeyboard(), click());

            waitForView(allOf(instanceOf(Spinner.class), hasSibling(withText(startsWith("Position")))))
                    .perform(scrollTo())
                    .perform(click());
            waitForView(withText("Role2")).perform(click());

            waitForView(withId(R.id.next_btn)).perform(click());

            waitForView(withId(R.id.next_btn)).check(matches(not(isDisplayed())));
            waitForView(withId(R.id.register_btn)).check(matches(isDisplayed()));

            waitForView(withId(R.id.prev_btn)).perform(click());

            waitForView(withId(R.id.next_btn)).check(matches(isDisplayed()));
            waitForView(withId(R.id.register_btn)).check(matches(not(isDisplayed())));
        }
    }


    @Test
    public void showErrorOnLastStepBeforeSubmit() throws Exception {

        if (!BuildConfig.ALLOW_REGISTER_USER) {
            return;
        }

        setRegisterSteps(REGISTER_STEPS_NORMAL);

        try (ActivityScenario<WelcomeActivity> scenario = ActivityScenario.launch(WelcomeActivity.class)) {
            waitForView(withId(R.id.welcome_register)).perform(scrollTo(), click());

            onEditTextWithinTextInputLayoutWithId(R.id.register_form_username_field)
                    .perform(closeSoftKeyboard(), scrollTo(), typeText("Username"));
            onEditTextWithinTextInputLayoutWithId(R.id.register_form_password_field)
                    .perform(closeSoftKeyboard(), scrollTo(), typeText("password1"));
            onEditTextWithinTextInputLayoutWithId(R.id.register_form_password_again_field)
                    .perform(closeSoftKeyboard(), scrollTo(), typeText("password1"));

            waitForView(withId(R.id.next_btn)).perform(closeSoftKeyboard(), click());

            waitForView(allOf(instanceOf(Spinner.class), hasSibling(withText(startsWith("Position")))))
                    .perform(scrollTo())
                    .perform(click());
            waitForView(withText("Role2")).perform(click());

            waitForView(withId(R.id.next_btn)).perform(click());

            waitForView(withId(R.id.register_btn)).perform(click());

            onErrorViewWithinTextInputLayoutWithId(R.id.register_form_firstname_field)
                    .check(matches(withText(R.string.field_required)));
        }
    }

    @Test
    public void keepDataWhenScreenStops() throws Exception {

        if (!BuildConfig.ALLOW_REGISTER_USER) {
            return;
        }

        try (ActivityScenario<WelcomeActivity> scenario = ActivityScenario.launch(WelcomeActivity.class)) {
            waitForView(withId(R.id.welcome_register)).perform(scrollTo(), click());

            onEditTextWithinTextInputLayoutWithId(R.id.register_form_username_field)
                    .perform(closeSoftKeyboard(), scrollTo(), typeText("Username"));
            onEditTextWithinTextInputLayoutWithId(R.id.register_form_password_field)
                    .perform(closeSoftKeyboard(), scrollTo(), typeText("password1"));
            onEditTextWithinTextInputLayoutWithId(R.id.register_form_password_again_field)
                    .perform(closeSoftKeyboard(), scrollTo(), typeText("password1"));

            openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getInstrumentation().getTargetContext());
            waitForView(withText(R.string.menu_settings)).perform(closeSoftKeyboard(), click());

            pressBack();

            onEditTextWithinTextInputLayoutWithId(R.id.register_form_username_field).check(matches(withText("Username")));
            onEditTextWithinTextInputLayoutWithId(R.id.register_form_password_field).check(matches(withText("password1")));
            onEditTextWithinTextInputLayoutWithId(R.id.register_form_password_again_field).check(matches(withText("password1")));

        }
    }

    @Test
    public void keepDataWhenScreenRotates() throws Exception {

        if (!BuildConfig.ALLOW_REGISTER_USER) {
            return;
        }

        try (ActivityScenario<WelcomeActivity> scenario = ActivityScenario.launch(WelcomeActivity.class)) {


            waitForView(withId(R.id.welcome_register)).perform(scrollTo(), click());

            onEditTextWithinTextInputLayoutWithId(R.id.register_form_username_field)
                    .perform(closeSoftKeyboard(), scrollTo(), typeText("Username"));
            onEditTextWithinTextInputLayoutWithId(R.id.register_form_password_field)
                    .perform(closeSoftKeyboard(), scrollTo(), typeText("password1"));
            onEditTextWithinTextInputLayoutWithId(R.id.register_form_password_again_field)
                    .perform(closeSoftKeyboard(), scrollTo(), typeText("password1")
                            , closeSoftKeyboard());

            UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
            device.setOrientationLeft();
            device.setOrientationNatural();

            onEditTextWithinTextInputLayoutWithId(R.id.register_form_username_field).check(matches(withText("Username")));
            onEditTextWithinTextInputLayoutWithId(R.id.register_form_password_field).check(matches(withText("password1")));
            onEditTextWithinTextInputLayoutWithId(R.id.register_form_password_again_field).check(matches(withText("password1")));


        }
    }
}
