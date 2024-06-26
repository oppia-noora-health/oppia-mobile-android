package androidTestFiles.features.prefs;

import static androidx.fragment.app.testing.FragmentScenario.launchInContainer;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isChecked;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isNotChecked;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidTestFiles.utils.UITestActionsUtils.waitForView;

import androidx.test.espresso.contrib.RecyclerViewActions;

import junit.framework.AssertionFailedError;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.fragments.prefs.NotificationsPrefsFragment;
import org.junit.Test;

public class ReminderPrefsTest {


    @Test
    public void showWarningIfZeroDaysSelected() throws Exception {

        launchInContainer(NotificationsPrefsFragment.class, null, R.style.Oppia_ToolbarTheme);

        waitForView(withId(androidx.preference.R.id.recycler_view))
                .perform(RecyclerViewActions.actionOnItem(
                        hasDescendant(withText(R.string.prefCoursesReminderDaysTitle)), click()));

        uncheckViewWithText(R.string.week_day_2);
        uncheckViewWithText(R.string.week_day_3);
        uncheckViewWithText(R.string.week_day_4);
        uncheckViewWithText(R.string.week_day_5);
        uncheckViewWithText(R.string.week_day_6);
        uncheckViewWithText(R.string.week_day_7);
        uncheckViewWithText(R.string.week_day_1);

        waitForView(withText(android.R.string.ok))
                .inRoot(isDialog())
                .check(matches(isDisplayed()))
                .perform(click());

        waitForView(withText(R.string.warning_reminder_at_least_one_day)).check(matches(isDisplayed()));


    }


    @Test
    public void showWarningIfMoreThanOneDayInWeeklyInterval() throws Exception {

        launchInContainer(NotificationsPrefsFragment.class, null, R.style.Oppia_ToolbarTheme);

        waitForView(withId(androidx.preference.R.id.recycler_view))
                .perform(RecyclerViewActions.actionOnItem(
                        hasDescendant(withText(R.string.prefCoursesReminderIntervalTitle)), click()));

        waitForView(withText(R.string.interval_weekly)).perform(click());

        waitForView(withId(androidx.preference.R.id.recycler_view))
                .perform(RecyclerViewActions.actionOnItem(
                        hasDescendant(withText(R.string.prefCoursesReminderDaysTitle)), click()));

        checkViewWithText(R.string.week_day_1);
        checkViewWithText(R.string.week_day_3);

        waitForView(withText(android.R.string.ok))
                .inRoot(isDialog())
                .check(matches(isDisplayed()))
                .perform(click());

        waitForView(withText(R.string.warning_reminder_weekly_just_one_day)).check(matches(isDisplayed()));
    }


    /**
     * Little hack to achieve uncheck if view it is already checked without throwing an error
     * @param stringId
     */
    private void uncheckViewWithText(int stringId) {
        try {
            waitForView(withText(stringId)).check(matches(isChecked()));
            waitForView(withText(stringId)).perform(click());
        } catch (AssertionFailedError e) {
            // It is already unchecked
        }
    }

    private void checkViewWithText(int stringId) {
        try {
            waitForView(withText(stringId)).check(matches(isNotChecked()));
            waitForView(withText(stringId)).perform(click());
        } catch (AssertionFailedError e) {
            // It is already unchecked
        }
    }

}
