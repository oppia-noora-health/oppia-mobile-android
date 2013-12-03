/* 
 * This file is part of OppiaMobile - http://oppia-mobile.org/
 * 
 * OppiaMobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * OppiaMobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with OppiaMobile. If not, see <http://www.gnu.org/licenses/>.
 */

package org.digitalcampus.oppia.activity;

import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.Callable;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.adapter.SectionListAdapter;
import org.digitalcampus.oppia.model.Activity;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.Section;
import org.digitalcampus.oppia.utils.UIUtils;
import org.digitalcampus.oppia.widgets.PageWidget;
import org.digitalcampus.oppia.widgets.QuizWidget;
import org.digitalcampus.oppia.widgets.ResourceWidget;
import org.digitalcampus.oppia.widgets.WidgetFactory;

import android.app.ActionBar;
import android.app.ActionBar.Tab;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class CourseActivity extends FragmentActivity implements ActionBar.TabListener {

	public static final String TAG = CourseActivity.class.getSimpleName();
	public static final String BASELINE_TAG = "BASELINE";
	private Section section;
	private Course course;
	private int currentActivityNo = 0;
	private WidgetFactory currentActivity;
	private SharedPreferences prefs;
	private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";
	private ArrayList<Activity> activities;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_course);
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setHomeButtonEnabled(true);

		prefs = PreferenceManager.getDefaultSharedPreferences(this);

		Bundle bundle = this.getIntent().getExtras();
		if (bundle != null) {
			section = (Section) bundle.getSerializable(Section.TAG);
			course = (Course) bundle.getSerializable(Course.TAG);
			currentActivityNo = (Integer) bundle.getSerializable(SectionListAdapter.TAG_PLACEHOLDER);
			if (bundle.getSerializable(CourseActivity.BASELINE_TAG) != null) {
				//this.isBaselineActivity = (Boolean) bundle.getSerializable(CourseActivity.BASELINE_TAG);
			}
			activities = section.getActivities();
			for (int i = 0; i < activities.size(); i++) {
				String title = section
						.getActivities()
						.get(i)
						.getTitle(
								prefs.getString(getString(R.string.prefs_language), Locale.getDefault().getLanguage()));
				actionBar.addTab(actionBar.newTab().setText(title).setTabListener(this));
			}
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		// Serialize the current tab position.
		outState.putInt(STATE_SELECTED_NAVIGATION_ITEM, getActionBar().getSelectedNavigationIndex());
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		// Restore the previously serialized current tab position.
		if (savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
			getActionBar().setSelectedNavigationItem(savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		setTitle(section.getTitle(prefs
				.getString(getString(R.string.prefs_language), Locale.getDefault().getLanguage())));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_course, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem item = (MenuItem) menu.findItem(R.id.menu_tts);
		/*if (ttsRunning) {
			item.setTitle(R.string.menu_stop_read_aloud);
		} else {
			item.setTitle(R.string.menu_read_aloud);
		}*/
		item.setTitle(R.string.menu_read_aloud);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.menu_language:
			createLanguageDialog();
			return true;
		case R.id.menu_help:
			startActivity(new Intent(this, HelpActivity.class));
			return true;
		case android.R.id.home:
			this.finish();
			return true;
		case R.id.menu_tts:
			/*if (myTTS == null && !ttsRunning) {
				// check for TTS data
				Intent checkTTSIntent = new Intent();
				checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
				startActivityForResult(checkTTSIntent, TTS_CHECK);
			} else if (myTTS != null && ttsRunning) {
				this.stopReading();
			} else {
				// TTS not installed so show message
				Toast.makeText(this, this.getString(R.string.error_tts_start), Toast.LENGTH_LONG).show();
			}*/
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}


	private void createLanguageDialog() {
		UIUtils ui = new UIUtils();
		ui.createLanguageDialog(this, course.getLangs(), prefs, new Callable<Boolean>() {
			public Boolean call() throws Exception {
				return true;
			}
		});
	}


	public WidgetFactory getCurrentActivity() {
		return this.currentActivity;
	}

	public void onTabSelected(Tab tab, android.app.FragmentTransaction ft){
		if (activities.get(tab.getPosition()).getActType().equals("page")) {
			Fragment fragment =  new PageWidget();
			Bundle args = new Bundle();
		    args.putSerializable(Activity.TAG,activities.get(tab.getPosition()));
		    args.putSerializable(Course.TAG,course);
		    fragment.setArguments(args);
			getSupportFragmentManager().beginTransaction().replace(R.id.activity_widget, fragment).commit();
		} else if (activities.get(this.currentActivityNo).getActType().equals("quiz")) {
			Fragment fragment = new QuizWidget(CourseActivity.this, course, activities.get(this.currentActivityNo));
		} else if (activities.get(this.currentActivityNo).getActType().equals("resource")) {
			Fragment fragment = new ResourceWidget(this, course, activities.get(this.currentActivityNo));
		}

	}

	public void onTabReselected(Tab tab, android.app.FragmentTransaction ft) {
		// TODO Auto-generated method stub
		
	}


	public void onTabUnselected(Tab tab, android.app.FragmentTransaction ft) {
		// TODO Auto-generated method stub
		
	}

}
