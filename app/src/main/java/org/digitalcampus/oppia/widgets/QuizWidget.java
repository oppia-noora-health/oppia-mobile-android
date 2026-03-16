package org.digitalcampus.oppia.widgets;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.mobile.quiz.Quiz;
import org.digitalcampus.mobile.quiz.model.QuizQuestion;
import org.digitalcampus.mobile.quiz.model.questiontypes.Description;
import org.digitalcampus.mobile.quiz.model.questiontypes.Essay;
import org.digitalcampus.oppia.activity.CourseActivity;
import org.digitalcampus.oppia.activity.CourseQuizAttemptsActivity;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.adapter.QuizAnswersFeedbackAdapter;
import org.digitalcampus.oppia.application.SessionManager;
import org.digitalcampus.oppia.database.DbHelper;
import org.digitalcampus.oppia.gamification.GamificationServiceDelegate;
import org.digitalcampus.oppia.model.Activity;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.QuizAnswerFeedback;
import org.digitalcampus.oppia.model.QuizStats;
import org.digitalcampus.oppia.utils.ui.QuizTourManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class QuizWidget extends AnswerWidget {

    public static final String TAG = QuizWidget.class.getSimpleName();

    public static QuizWidget newInstance(Activity activity, Course course, boolean isBaseline) {
        QuizWidget myFragment = new QuizWidget();

        Bundle args = new Bundle();
        args.putSerializable(Activity.TAG, activity);
        args.putSerializable(Course.TAG, course);
        args.putBoolean(CourseActivity.BASELINE_TAG, isBaseline);
        myFragment.setArguments(args);

        return myFragment;
    }

    public QuizWidget() { }

    @Override
    int getContentAvailability(boolean afterAttempt) {
        if (isUserOverLimitedAttempts(afterAttempt)){
            return R.string.widget_quiz_unavailable_attempts;
        }
        DbHelper db = DbHelper.getInstance(getActivity());
        long userId = db.getUserId(SessionManager.getUsername(getActivity()));

        switch (this.quiz.getAvailability()) {
            case Quiz.AVAILABILITY_ALWAYS:
                return QUIZ_AVAILABLE;
            case Quiz.AVAILABILITY_SECTION:
                return db.isPreviousSectionActivitiesCompleted(activity, userId) ? QUIZ_AVAILABLE : R.string.widget_quiz_unavailable_section;
            case Quiz.AVAILABILITY_COURSE:
                return db.isPreviousCourseActivitiesCompleted(activity, userId) ? QUIZ_AVAILABLE : R.string.widget_quiz_unavailable_course;
            default:
                return QUIZ_AVAILABLE;
        }
    }

    @Override
    String getAnswerWidgetType() {
        return getString(R.string.quiz);
    }

    @Override
    protected void showContentUnavailableRationale(String unavailabilityReasonString) {
        super.showContentUnavailableRationale(unavailabilityReasonString);
        QuizStats quizStats = attemptsRepository.getQuizAttemptStats(getActivity(), course.getCourseId(), activity.getDigest());
        quizStats.setQuizTitle(activity.getTitle(prefLang));
        if (quizStats.isAttempted()) {
            Button button = getView().findViewById(R.id.btn_quiz_unavailable);
            button.setVisibility(View.VISIBLE);
            button.setText(R.string.view_your_previous_attempts);
            button.setOnClickListener(v -> {
                Intent i = new Intent(getActivity(), CourseQuizAttemptsActivity.class);
                i.putExtra(QuizStats.TAG, quizStats);
                i.putExtra(CourseQuizAttemptsActivity.SHOW_ATTEMPT_BUTTON, false);
                startActivity(i);
            });
        }
    }

    @Override
    String getFinishButtonLabel() {
        return getString(R.string.widget_quiz_getresults);
    }

    @Override
    void showResultsInfo() {
        TextView title = getView().findViewById(R.id.quiz_results_score);
        ViewGroup info = getView().findViewById(R.id.quiz_stats);

        if (isEssayQuiz()) {
            // Hide score
            title.setVisibility(View.GONE);

            // Hide stats card
            info.setVisibility(View.GONE);

            // Show thank you message
            TextView thankYouMsg = getView().findViewById(R.id.quiz_results_general_feedback);
            thankYouMsg.setVisibility(View.VISIBLE);
            thankYouMsg.setText(getString(R.string.widget_quiz_essay_message));

        } else {

            title.setText(getString(R.string.widget_quiz_results_score, this.getPercentScore()));

            if (!isBaseline) {
                info.setVisibility(View.VISIBLE);

                QuizStats stats = attemptsRepository.getQuizAttemptStats(getContext(), course.getCourseId(), activity.getDigest());
                // We take into account the current quiz (not saved yet)
                int numAttempts = stats.getNumAttempts();
                float average = ((stats.getAverageScore() * numAttempts) + quiz.getUserscore()) / (numAttempts + 1);
                stats.setMaxScore(Math.max(quiz.getMaxscore(), stats.getMaxScore()));
                stats.setNumAttempts(numAttempts + 1);
                stats.setUserScore(Math.max(quiz.getUserscore(), stats.getUserScore()));
                stats.setAverageScore(average);
                showStats(info, stats);
            }

            if (!quiz.mustShowQuizResultsAtEnd()) {
                getView().findViewById(R.id.recycler_quiz_results_feedback).setVisibility(View.GONE);
            }
        }

        // Start Result Tour (only once)
        if (!isBaseline) {
            startResultTour();
        }
    }

    @Override
    boolean shouldShowInitialInfo() {
        return !this.isBaseline;
    }

    private void showStats(ViewGroup infoContainer, QuizStats stats){
        TextView average = infoContainer.findViewById(R.id.highlight_average);
        TextView best = infoContainer.findViewById(R.id.highlight_best);
        TextView numAttempts = infoContainer.findViewById(R.id.highlight_attempted);
        TextView infoAttempts = infoContainer.findViewById(R.id.info_num_attempts);
        TextView threshold = infoContainer.findViewById(R.id.info_threshold);

        numAttempts.setText(String.valueOf(stats.getNumAttempts()));
        threshold.setText(getString(R.string.widget_quiz_pass_threshold, quiz.getPassThreshold()));

        if (quiz.limitAttempts()){
            int attemptsLeft = Math.max(quiz.getMaxAttempts() - stats.getNumAttempts(), 0);
            infoAttempts.setText(getString(R.string.quiz_attempts_left, quiz.getMaxAttempts(), attemptsLeft));
        }
        else{
            infoAttempts.setText(R.string.quiz_attempts_unlimited);
        }

        if (stats.getNumAttempts() == 0){
            average.setText("-");
            best.setText("-");
        }
        else{
            average.setText(stats.getAveragePercent() + "%");
            best.setText(stats.getPercent() + "%");
        }
    }

    @Override
    void loadInitialInfo(ViewGroup infoContainer) {
        if (isEssayQuiz()) {
            // ✅ Don’t show initial screen, directly move to essay flow
            checkPasswordProtectionAndShowQuestion();
            return;
        }

        infoContainer.removeAllViews();
        ViewGroup info = (ViewGroup) View.inflate(infoContainer.getContext(), R.layout.view_quiz_info, infoContainer);
        ProgressBar thresholdBar = info.findViewById(R.id.threshold_bar);
        TextView numQuestions = info.findViewById(R.id.info_num_questions);

        numQuestions.setText(getString(R.string.widget_quiz_num_questions, quiz.getTotalNoQuestions()));
        thresholdBar.setProgress(quiz.getPassThreshold());

        TextView tvTitle = info.findViewById(R.id.tv_quiz_title);
        String currentLang = prefs.getString(PrefsActivity.PREF_CONTENT_LANGUAGE, Locale.getDefault().getLanguage());
        tvTitle.setText(quiz.getTitle(currentLang));

        info.findViewById(R.id.take_quiz_btn).setOnClickListener(view -> {
            checkPasswordProtectionAndShowQuestion();
        });

        QuizStats stats = attemptsRepository.getQuizAttemptStats(getContext(), course.getCourseId(), activity.getDigest());
        showStats(info, stats);
    }

    @Override
    void showBaselineResultMessage() {
        TextView baselineText = getView().findViewById(R.id.quiz_results_baseline);
        baselineText.setText(getString(R.string.widget_quiz_baseline_completed));
        baselineText.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean getActivityCompleted() {
        int passThreshold;
        Log.d(TAG, "Threshold:" + quiz.getPassThreshold() );
        if (quiz.getPassThreshold() >= 0){
            passThreshold = quiz.getPassThreshold();
        } else {
            passThreshold = Quiz.QUIZ_DEFAULT_PASS_THRESHOLD;
        }
        Log.d(TAG, "Percent:" + this.getPercentScore() );
        return (isOnResultsPage && this.getPercentScore() >= passThreshold);
    }

    @Override
    void saveAttemptTracker() {
        long timetaken = this.getSpentTime();
        new GamificationServiceDelegate(getActivity())
                .createActivityIntent(course, activity, getActivityCompleted(), isBaseline)
                .registerQuizAttemptEvent(timetaken, quiz, this.getPercentScore());
    }

    @Override
    void showAnswersFeedback() {
        RecyclerView recyclerQuestionFeedbackLV = getView().findViewById(R.id.recycler_quiz_results_feedback);
        recyclerQuestionFeedbackLV.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        ArrayList<QuizAnswerFeedback> quizAnswersFeedback = new ArrayList<>();
        List<QuizQuestion> questions = this.quiz.getQuestions();
        for(QuizQuestion q: questions){
            if(!(q instanceof Description)){
                QuizAnswerFeedback qf = new QuizAnswerFeedback();
                qf.setScore(q.getScoreAsPercent());
                qf.setQuestionText(q.getTitle(prefLang));
                qf.setUserResponse(q.getUserResponses());

                // detect if essay
                if (q instanceof Essay) {
                    qf.setEssay(true); // <-- add this method in QuizAnswerFeedback
                }

                String feedbackText = q.getFeedback(prefLang);
                qf.setFeedbackText(feedbackText.replace("&amp;gt;","<"));
                quizAnswersFeedback.add(qf);
            }
        }
        QuizAnswersFeedbackAdapter adapterQuizFeedback = new QuizAnswersFeedbackAdapter(getActivity(), quizAnswersFeedback);
        recyclerQuestionFeedbackLV.setAdapter(adapterQuizFeedback);
    }

    private boolean isEssayQuiz() {
        List<QuizQuestion> questions = quiz.getQuestions();
        if (questions == null || questions.isEmpty()) return false;
        for (QuizQuestion q : questions) if (!(q instanceof Essay)) return false;
        return true;
    }

    private void startResultTour() {
        View root = getView();
        if (root == null) return;

        View resultContainer = root.findViewById(R.id.widget_quiz_results);
        if (resultContainer == null) return;

        Button retakeBtn = resultContainer.findViewById(R.id.quiz_results_button);
        Button continueBtn = resultContainer.findViewById(R.id.quiz_exit_button);

        if (retakeBtn == null || continueBtn == null) {
            // Retry if buttons not ready
            resultContainer.postDelayed(this::startResultTour, 300);
            return;
        }

        // Wait for first button to be laid out
        retakeBtn.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                retakeBtn.getViewTreeObserver().removeOnPreDrawListener(this);

                QuizTourManager tourManager = new QuizTourManager(getActivity());
                tourManager.startResultTourIfFirstLaunch(resultContainer, retakeBtn, continueBtn);
                return true;
            }
        });
    }

}
