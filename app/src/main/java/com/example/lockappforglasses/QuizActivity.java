package com.example.lockappforglasses;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class QuizActivity extends AppCompatActivity {

    private static final String QUESTION_BANK_COLLECTION = "qb_questions_v1";
    private static final String QUESTION_BANK_LISTS_COLLECTION = "qb_lists_v1";
    private static final String SUBMISSIONS_COLLECTION = "qb_quiz_submissions_v1";

    private Spinner spinnerClass;
    private Spinner spinnerSubject;
    private Spinner spinnerDifficulty;
    private LinearLayout llChapters;
    private LinearLayout setupPanel;
    private LinearLayout questionPanel;
    private LinearLayout reviewPanel;
    private LinearLayout llAnswerArea;
    private TextView tvLoggedInStudent;
    private TextView tvSetupStatus;
    private TextView tvQuestionNumber;
    private TextView tvQuestionPrompt;
    private Button buttonStartQuiz;
    private Button buttonNextQuestion;
    private Button buttonSubmitQuiz;

    private final List<Question> allPublishedQuestions = new ArrayList<>();
    private final List<Question> quizQuestions = new ArrayList<>();
    private final Map<String, Answer> answers = new HashMap<>();
    private final List<String> selectedChapters = new ArrayList<>();
    private int currentQuestionIndex = 0;
    private boolean quizStarted = false;
    private boolean questionBankListMode = false;
    private String questionBankListId = "";
    private String questionBankListName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        setContentView(R.layout.activity_quiz);

        spinnerClass = findViewById(R.id.spinnerClass);
        spinnerSubject = findViewById(R.id.spinnerSubject);
        spinnerDifficulty = findViewById(R.id.spinnerDifficulty);
        llChapters = findViewById(R.id.llChapters);
        setupPanel = findViewById(R.id.setupPanel);
        questionPanel = findViewById(R.id.questionPanel);
        reviewPanel = findViewById(R.id.reviewPanel);
        llAnswerArea = findViewById(R.id.llAnswerArea);
        tvLoggedInStudent = findViewById(R.id.tvLoggedInStudent);
        tvSetupStatus = findViewById(R.id.tvSetupStatus);
        tvQuestionNumber = findViewById(R.id.tvQuestionNumber);
        tvQuestionPrompt = findViewById(R.id.tvQuestionPrompt);
        buttonStartQuiz = findViewById(R.id.buttonStartQuiz);
        buttonNextQuestion = findViewById(R.id.buttonNextQuestion);
        buttonSubmitQuiz = findViewById(R.id.buttonSubmitQuiz);

        findViewById(R.id.buttonCloseQuiz).setOnClickListener(v -> finish());
        findViewById(R.id.buttonPastSubmissions).setOnClickListener(v -> loadPastSubmissions());
        findViewById(R.id.buttonLogout).setOnClickListener(v -> logoutStudent());
        buttonStartQuiz.setOnClickListener(v -> startQuiz());
        buttonNextQuestion.setOnClickListener(v -> goToNextQuestion());
        buttonSubmitQuiz.setOnClickListener(v -> submitQuiz());

        spinnerClass.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                refreshSubjects();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        spinnerSubject.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                refreshChapters();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        showLoggedInStudent();
        questionBankListId = ((MyApp) getApplicationContext()).getCurrentQuestionBankListId();
        questionBankListMode = !TextUtils.isEmpty(questionBankListId);
        if (questionBankListMode) {
            showQuestionBankListSetup();
            loadQuestionBankList();
        } else {
            loadQuestionBank();
        }
    }

    private void showLoggedInStudent() {
        MyApp app = (MyApp) getApplicationContext();
        String studentName = app.getCurrentStudentName();
        if (TextUtils.isEmpty(studentName)) {
            studentName = app.getCurrentStudentAdmissionNo();
        }
        tvLoggedInStudent.setText(TextUtils.isEmpty(studentName) ? "" : "Student: " + studentName);
    }

    private void logoutStudent() {
        ((MyApp) getApplicationContext()).clearTempString();
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showQuestionBankListSetup() {
        for (int i = 0; i < setupPanel.getChildCount(); i++) {
            View child = setupPanel.getChildAt(i);
            child.setVisibility(child == buttonStartQuiz || child == tvSetupStatus ? View.VISIBLE : View.GONE);
        }
        buttonStartQuiz.setEnabled(false);
        tvSetupStatus.setText("Loading assigned quiz...");
    }

    private void loadQuestionBankList() {
        FirebaseFirestore.getInstance()
                .collection(QUESTION_BANK_LISTS_COLLECTION)
                .document(questionBankListId)
                .get()
                .addOnSuccessListener(this::loadQuestionsFromList)
                .addOnFailureListener(e -> {
                    tvSetupStatus.setText("Unable to load assigned quiz.");
                    Toast.makeText(this, "Unable to load assigned quiz", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadQuestionsFromList(DocumentSnapshot listDocument) {
        if (!listDocument.exists()) {
            tvSetupStatus.setText("Assigned quiz was not found.");
            return;
        }

        questionBankListName = listDocument.getString("name");
        List<Object> rawQuestionIds = (List<Object>) listDocument.get("questionIds");
        if (rawQuestionIds == null || rawQuestionIds.isEmpty()) {
            tvSetupStatus.setText("Assigned quiz has no questions.");
            return;
        }

        ArrayList<String> questionIds = new ArrayList<>();
        ArrayList<Task<DocumentSnapshot>> tasks = new ArrayList<>();
        for (Object rawQuestionId : rawQuestionIds) {
            if (rawQuestionId == null || TextUtils.isEmpty(rawQuestionId.toString())) {
                continue;
            }
            String questionId = rawQuestionId.toString();
            questionIds.add(questionId);
            tasks.add(FirebaseFirestore.getInstance()
                    .collection(QUESTION_BANK_COLLECTION)
                    .document(questionId)
                    .get());
        }

        if (tasks.isEmpty()) {
            tvSetupStatus.setText("Assigned quiz has no valid questions.");
            return;
        }

        Tasks.whenAllSuccess(tasks)
                .addOnSuccessListener(results -> showQuestionBankListQuestions(questionIds, results))
                .addOnFailureListener(e -> {
                    tvSetupStatus.setText("Unable to load assigned quiz questions.");
                    Toast.makeText(this, "Unable to load assigned questions", Toast.LENGTH_SHORT).show();
                });
    }

    private void showQuestionBankListQuestions(List<String> questionIds, List<Object> results) {
        HashMap<String, Question> questionsById = new HashMap<>();
        for (Object result : results) {
            if (result instanceof DocumentSnapshot) {
                DocumentSnapshot document = (DocumentSnapshot) result;
                Question question = Question.fromDocument(document);
                if (question != null) {
                    questionsById.put(question.id, question);
                }
            }
        }

        allPublishedQuestions.clear();
        for (String questionId : questionIds) {
            Question question = questionsById.get(questionId);
            if (question != null) {
                allPublishedQuestions.add(question);
            }
        }

        if (allPublishedQuestions.isEmpty()) {
            tvSetupStatus.setText("Assigned quiz has no readable questions.");
            return;
        }

        buttonStartQuiz.setEnabled(true);
        tvSetupStatus.setText("Assigned quiz ready: " + getQuestionBankListDisplayName());
    }

    private void loadQuestionBank() {
        tvSetupStatus.setText("Loading question bank...");
        FirebaseFirestore.getInstance()
                .collection(QUESTION_BANK_COLLECTION)
                .whereEqualTo("status", "published")
                .get()
                .addOnSuccessListener(this::showQuestionSetup)
                .addOnFailureListener(e -> {
                    tvSetupStatus.setText("Unable to load question bank.");
                    Toast.makeText(this, "Unable to load questions", Toast.LENGTH_SHORT).show();
                });
    }

    private void showQuestionSetup(QuerySnapshot querySnapshot) {
        allPublishedQuestions.clear();
        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
            Question question = Question.fromDocument(document);
            if (question != null) {
                allPublishedQuestions.add(question);
            }
        }

        setSpinnerItems(spinnerClass, getUniqueValues("className", null, null));
        setSpinnerItems(spinnerDifficulty, getDifficultyValues());
        refreshSubjects();
        tvSetupStatus.setText(allPublishedQuestions.size() + " published questions available.");
    }

    private void refreshSubjects() {
        if (allPublishedQuestions.isEmpty()) {
            return;
        }

        String selectedClass = getSelectedSpinnerValue(spinnerClass);
        setSpinnerItems(spinnerSubject, getUniqueValues("subject", selectedClass, null));
        refreshChapters();
    }

    private void refreshChapters() {
        if (allPublishedQuestions.isEmpty()) {
            return;
        }

        String selectedClass = getSelectedSpinnerValue(spinnerClass);
        String selectedSubject = getSelectedSpinnerValue(spinnerSubject);
        showChapterChoices(getUniqueValues("chapter", selectedClass, selectedSubject));
    }

    private List<String> getUniqueValues(String field, String className, String subject) {
        LinkedHashSet<String> values = new LinkedHashSet<>();
        for (Question question : allPublishedQuestions) {
            if (!TextUtils.isEmpty(className) && !className.equals(question.className)) {
                continue;
            }
            if (!TextUtils.isEmpty(subject) && !subject.equals(question.subject)) {
                continue;
            }

            if ("className".equals(field) && !TextUtils.isEmpty(question.className)) {
                values.add(question.className);
            } else if ("subject".equals(field) && !TextUtils.isEmpty(question.subject)) {
                values.add(question.subject);
            } else if ("chapter".equals(field) && !TextUtils.isEmpty(question.chapter)) {
                values.add(question.chapter);
            }
        }
        return new ArrayList<>(values);
    }

    private List<String> getDifficultyValues() {
        ArrayList<String> values = new ArrayList<>();
        values.add("Easy");
        values.add("Medium");
        values.add("Hard");
        values.add("Very Hard");
        return values;
    }

    private void setSpinnerItems(Spinner spinner, List<String> values) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, values);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private String getSelectedSpinnerValue(Spinner spinner) {
        Object selectedItem = spinner.getSelectedItem();
        return selectedItem == null ? "" : selectedItem.toString();
    }

    private void showChapterChoices(List<String> chapters) {
        llChapters.removeAllViews();
        selectedChapters.clear();
        for (String chapter : chapters) {
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(chapter);
            checkBox.setTextColor(getResources().getColor(R.color.black));
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectedChapters.add(chapter);
                } else {
                    selectedChapters.remove(chapter);
                }
            });
            llChapters.addView(checkBox);
        }
    }

    private void startQuiz() {
        if (questionBankListMode) {
            startQuestionBankListQuiz();
            return;
        }

        String className = getSelectedSpinnerValue(spinnerClass);
        String subject = getSelectedSpinnerValue(spinnerSubject);
        String difficulty = getSelectedSpinnerValue(spinnerDifficulty);
        if (TextUtils.isEmpty(className) || TextUtils.isEmpty(subject) || TextUtils.isEmpty(difficulty)) {
            Toast.makeText(this, "Choose class, subject and difficulty", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedChapters.isEmpty()) {
            Toast.makeText(this, "Choose at least one chapter", Toast.LENGTH_SHORT).show();
            return;
        }

        quizQuestions.clear();
        answers.clear();
        for (Question question : allPublishedQuestions) {
            if (className.equals(question.className)
                    && subject.equals(question.subject)
                    && difficulty.equals(question.difficulty)
                    && selectedChapters.contains(question.chapter)) {
                quizQuestions.add(question);
            }
        }

        if (quizQuestions.isEmpty()) {
            Toast.makeText(this, "No questions found for this selection", Toast.LENGTH_SHORT).show();
            return;
        }

        Collections.shuffle(quizQuestions);
        beginQuiz();
    }

    private void startQuestionBankListQuiz() {
        quizQuestions.clear();
        answers.clear();
        quizQuestions.addAll(allPublishedQuestions);
        if (quizQuestions.isEmpty()) {
            Toast.makeText(this, "Assigned quiz has no questions", Toast.LENGTH_SHORT).show();
            return;
        }
        beginQuiz();
    }

    private void beginQuiz() {
        currentQuestionIndex = 0;
        quizStarted = true;
        setupPanel.setVisibility(View.GONE);
        reviewPanel.setVisibility(View.GONE);
        questionPanel.setVisibility(View.VISIBLE);
        showCurrentQuestion();
    }

    private void showCurrentQuestion() {
        Question question = quizQuestions.get(currentQuestionIndex);
        tvQuestionNumber.setText("Question " + (currentQuestionIndex + 1) + " of " + quizQuestions.size());
        tvQuestionPrompt.setText(fromHtml(question.promptHtml));
        llAnswerArea.removeAllViews();
        buttonNextQuestion.setEnabled(currentQuestionIndex < quizQuestions.size() - 1);

        if ("mcq".equals(question.type)) {
            showMcqAnswer(question);
        } else if ("true_false".equals(question.type)) {
            showTrueFalseAnswer(question);
        } else if ("fib".equals(question.type)) {
            showFibAnswer(question);
        } else {
            showShortAnswer(question);
        }
    }

    private void showMcqAnswer(Question question) {
        Answer previous = answers.get(question.id);
        Set<Integer> previousSelection = previous == null ? new HashSet<>() : previous.selectedOptions;
        for (int i = 0; i < question.options.size(); i++) {
            QuestionOption option = question.options.get(i);
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(fromHtml(option.html));
            checkBox.setTextColor(getResources().getColor(R.color.black));
            checkBox.setChecked(previousSelection.contains(i));
            final int optionIndex = i;
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> saveMcqAnswer(question, optionIndex, isChecked));
            llAnswerArea.addView(checkBox);
        }
    }

    private void saveMcqAnswer(Question question, int optionIndex, boolean isChecked) {
        Answer answer = getOrCreateAnswer(question);
        if (isChecked) {
            answer.selectedOptions.add(optionIndex);
        } else {
            answer.selectedOptions.remove(optionIndex);
        }
        answer.displayAnswer = getSelectedOptionLabels(question, answer.selectedOptions);
    }

    private void showTrueFalseAnswer(Question question) {
        Answer previous = answers.get(question.id);
        RadioGroup radioGroup = new RadioGroup(this);
        radioGroup.setOrientation(RadioGroup.VERTICAL);

        RadioButton trueButton = new RadioButton(this);
        trueButton.setText("True");
        trueButton.setTextColor(getResources().getColor(R.color.black));
        trueButton.setId(1);
        radioGroup.addView(trueButton);

        RadioButton falseButton = new RadioButton(this);
        falseButton.setText("False");
        falseButton.setTextColor(getResources().getColor(R.color.black));
        falseButton.setId(2);
        radioGroup.addView(falseButton);

        if (previous != null && previous.trueFalseAnswer != null) {
            radioGroup.check(previous.trueFalseAnswer ? 1 : 2);
        }

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            Answer answer = getOrCreateAnswer(question);
            answer.trueFalseAnswer = checkedId == 1;
            answer.displayAnswer = answer.trueFalseAnswer ? "True" : "False";
        });
        llAnswerArea.addView(radioGroup);
    }

    private void showFibAnswer(Question question) {
        Answer previous = answers.get(question.id);
        for (int i = 0; i < question.fibBanks.size(); i++) {
            FibBank bank = question.fibBanks.get(i);
            TextView label = new TextView(this);
            label.setText(bank.label);
            label.setTextColor(getResources().getColor(R.color.black));
            label.setPadding(0, 8, 0, 0);
            llAnswerArea.addView(label);

            EditText editText = new EditText(this);
            editText.setSingleLine(true);
            if (previous != null && previous.fibAnswers.size() > i) {
                editText.setText(previous.fibAnswers.get(i));
            }
            final int answerIndex = i;
            editText.setOnFocusChangeListener((v, hasFocus) -> {
                if (!hasFocus) {
                    saveFibAnswer(question, answerIndex, editText.getText().toString());
                }
            });
            editText.setOnEditorActionListener((v, actionId, event) -> {
                saveFibAnswer(question, answerIndex, editText.getText().toString());
                return false;
            });
            llAnswerArea.addView(editText);
        }
    }

    private void saveVisibleFibAnswers(Question question) {
        if (!"fib".equals(question.type)) {
            return;
        }
        int editTextIndex = 0;
        for (int i = 0; i < llAnswerArea.getChildCount(); i++) {
            View child = llAnswerArea.getChildAt(i);
            if (child instanceof EditText) {
                saveFibAnswer(question, editTextIndex, ((EditText) child).getText().toString());
                editTextIndex++;
            }
        }
    }

    private void saveFibAnswer(Question question, int answerIndex, String value) {
        Answer answer = getOrCreateAnswer(question);
        while (answer.fibAnswers.size() <= answerIndex) {
            answer.fibAnswers.add("");
        }
        answer.fibAnswers.set(answerIndex, value.trim());
        answer.displayAnswer = TextUtils.join(", ", answer.fibAnswers);
    }

    private void showShortAnswer(Question question) {
        Answer previous = answers.get(question.id);
        EditText editText = new EditText(this);
        editText.setMinLines(4);
        editText.setGravity(android.view.Gravity.TOP);
        if (previous != null) {
            editText.setText(previous.shortAnswer);
        }
        editText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                saveShortAnswer(question, editText.getText().toString());
            }
        });
        llAnswerArea.addView(editText);
    }

    private void saveVisibleShortAnswer(Question question) {
        if (!"short_answer".equals(question.type)) {
            return;
        }
        for (int i = 0; i < llAnswerArea.getChildCount(); i++) {
            View child = llAnswerArea.getChildAt(i);
            if (child instanceof EditText) {
                saveShortAnswer(question, ((EditText) child).getText().toString());
                return;
            }
        }
    }

    private void saveShortAnswer(Question question, String value) {
        Answer answer = getOrCreateAnswer(question);
        answer.shortAnswer = value.trim();
        answer.displayAnswer = answer.shortAnswer;
    }

    private Answer getOrCreateAnswer(Question question) {
        Answer answer = answers.get(question.id);
        if (answer == null) {
            answer = new Answer(question.id, question.type);
            answers.put(question.id, answer);
        }
        return answer;
    }

    private void goToNextQuestion() {
        saveVisibleFreeTextAnswer();
        if (currentQuestionIndex < quizQuestions.size() - 1) {
            currentQuestionIndex++;
            showCurrentQuestion();
        }
    }

    private void saveVisibleFreeTextAnswer() {
        if (!quizStarted || quizQuestions.isEmpty()) {
            return;
        }
        Question question = quizQuestions.get(currentQuestionIndex);
        saveVisibleFibAnswers(question);
        saveVisibleShortAnswer(question);
    }

    private void submitQuiz() {
        if (!quizStarted) {
            return;
        }
        saveVisibleFreeTextAnswer();

        MyApp app = (MyApp) getApplicationContext();
        String classroomId = app.getCurrentClassCode();
        String sectionId = app.getCurrentSectionId();
        String admissionNo = app.getCurrentStudentAdmissionNo();
        if (TextUtils.isEmpty(classroomId) || TextUtils.isEmpty(sectionId) || TextUtils.isEmpty(admissionNo)) {
            Toast.makeText(this, "Student verification is missing", Toast.LENGTH_SHORT).show();
            return;
        }

        ArrayList<Map<String, Object>> answerPayload = new ArrayList<>();
        int correctCount = 0;
        int gradableCount = 0;
        for (Question question : quizQuestions) {
            Answer answer = answers.get(question.id);
            if (answer == null || answer.isEmpty()) {
                continue;
            }
            Boolean isCorrect = gradeAnswer(question, answer);
            if (isCorrect != null) {
                gradableCount++;
                if (isCorrect) {
                    correctCount++;
                }
            }
            answerPayload.add(buildAnswerPayload(question, answer, isCorrect));
        }

        HashMap<String, Object> submission = new HashMap<>();
        submission.put("classroomId", classroomId);
        submission.put("sectionId", sectionId);
        submission.put("admissionNo", admissionNo);
        submission.put("studentName", app.getCurrentStudentName());
        submission.put("studentKey", getStudentKey(sectionId, admissionNo));
        submission.put("questionBankListId", questionBankListMode ? questionBankListId : null);
        submission.put("questionBankListName", questionBankListMode ? questionBankListName : null);
        submission.put("className", getSubmissionClassName());
        submission.put("subject", getSubmissionSubject());
        submission.put("chapters", getSubmissionChapters());
        submission.put("difficulty", getSubmissionDifficulty());
        submission.put("questionCount", quizQuestions.size());
        submission.put("answeredCount", answerPayload.size());
        submission.put("gradableCount", gradableCount);
        submission.put("correctCount", correctCount);
        submission.put("answers", answerPayload);
        submission.put("submittedAt", FieldValue.serverTimestamp());
        submission.put("submittedAtMillis", System.currentTimeMillis());

        final int finalCorrectCount = correctCount;
        final int finalGradableCount = gradableCount;
        buttonSubmitQuiz.setEnabled(false);
        FirebaseFirestore.getInstance()
                .collection(SUBMISSIONS_COLLECTION)
                .document(getSubmissionDocumentId(classroomId, admissionNo))
                .set(submission)
                .addOnSuccessListener(documentReference -> showSubmissionReview(answerPayload, finalCorrectCount, finalGradableCount))
                .addOnFailureListener(e -> {
                    buttonSubmitQuiz.setEnabled(true);
                    Toast.makeText(this, "Submission saved locally and will sync when online", Toast.LENGTH_LONG).show();
                });
    }

    private String getSubmissionClassName() {
        if (!questionBankListMode) {
            return getSelectedSpinnerValue(spinnerClass);
        }
        return firstNonEmptyValue("className");
    }

    private String getSubmissionSubject() {
        if (!questionBankListMode) {
            return getSelectedSpinnerValue(spinnerSubject);
        }
        return firstNonEmptyValue("subject");
    }

    private String getSubmissionDifficulty() {
        if (!questionBankListMode) {
            return getSelectedSpinnerValue(spinnerDifficulty);
        }
        return firstNonEmptyValue("difficulty");
    }

    private ArrayList<String> getSubmissionChapters() {
        if (!questionBankListMode) {
            return new ArrayList<>(selectedChapters);
        }

        LinkedHashSet<String> chapters = new LinkedHashSet<>();
        for (Question question : quizQuestions) {
            if (!TextUtils.isEmpty(question.chapter)) {
                chapters.add(question.chapter);
            }
        }
        return new ArrayList<>(chapters);
    }

    private String firstNonEmptyValue(String field) {
        for (Question question : quizQuestions) {
            if ("className".equals(field) && !TextUtils.isEmpty(question.className)) {
                return question.className;
            }
            if ("subject".equals(field) && !TextUtils.isEmpty(question.subject)) {
                return question.subject;
            }
            if ("difficulty".equals(field) && !TextUtils.isEmpty(question.difficulty)) {
                return question.difficulty;
            }
        }
        return "";
    }

    private String getQuestionBankListDisplayName() {
        return TextUtils.isEmpty(questionBankListName) ? questionBankListId : questionBankListName;
    }

    private Map<String, Object> buildAnswerPayload(Question question, Answer answer, Boolean isCorrect) {
        HashMap<String, Object> payload = new HashMap<>();
        payload.put("questionId", question.id);
        payload.put("type", question.type);
        payload.put("promptHtml", question.promptHtml);
        payload.put("displayAnswer", answer.displayAnswer);
        payload.put("isCorrect", isCorrect);
        payload.put("correctAnswer", getCorrectAnswerText(question));
        payload.put("selectedOptions", new ArrayList<>(answer.selectedOptions));
        payload.put("fibAnswers", new ArrayList<>(answer.fibAnswers));
        payload.put("trueFalseAnswer", answer.trueFalseAnswer);
        payload.put("shortAnswer", answer.shortAnswer);
        return payload;
    }

    private Boolean gradeAnswer(Question question, Answer answer) {
        if ("mcq".equals(question.type)) {
            Set<Integer> correctOptions = new HashSet<>();
            for (int i = 0; i < question.options.size(); i++) {
                if (question.options.get(i).correct) {
                    correctOptions.add(i);
                }
            }
            return correctOptions.equals(answer.selectedOptions);
        }
        if ("true_false".equals(question.type)) {
            return answer.trueFalseAnswer != null && answer.trueFalseAnswer == question.trueAnswer;
        }
        if ("fib".equals(question.type)) {
            if (answer.fibAnswers.size() < question.fibBanks.size()) {
                return false;
            }
            for (int i = 0; i < question.fibBanks.size(); i++) {
                String entered = normalizeAnswer(answer.fibAnswers.get(i));
                boolean matched = false;
                for (String accepted : question.fibBanks.get(i).answers) {
                    if (entered.equals(normalizeAnswer(accepted))) {
                        matched = true;
                        break;
                    }
                }
                if (!matched) {
                    return false;
                }
            }
            return true;
        }
        return null;
    }

    private String getCorrectAnswerText(Question question) {
        if ("mcq".equals(question.type)) {
            Set<Integer> correctOptions = new HashSet<>();
            for (int i = 0; i < question.options.size(); i++) {
                if (question.options.get(i).correct) {
                    correctOptions.add(i);
                }
            }
            return getSelectedOptionLabels(question, correctOptions);
        }
        if ("true_false".equals(question.type)) {
            return question.trueAnswer ? "True" : "False";
        }
        if ("fib".equals(question.type)) {
            ArrayList<String> labels = new ArrayList<>();
            for (FibBank bank : question.fibBanks) {
                labels.add(bank.label + ": " + TextUtils.join(" / ", bank.answers));
            }
            return TextUtils.join(", ", labels);
        }
        return htmlToPlainText(question.shortAnswerHtml);
    }

    private String getSelectedOptionLabels(Question question, Set<Integer> selectedOptions) {
        ArrayList<String> labels = new ArrayList<>();
        for (Integer index : selectedOptions) {
            if (index >= 0 && index < question.options.size()) {
                labels.add(htmlToPlainText(question.options.get(index).html));
            }
        }
        return TextUtils.join(", ", labels);
    }

    private String normalizeAnswer(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.US);
    }

    private void showSubmissionReview(List<Map<String, Object>> answerPayload, int correctCount, int gradableCount) {
        quizStarted = false;
        setupPanel.setVisibility(View.GONE);
        questionPanel.setVisibility(View.GONE);
        reviewPanel.setVisibility(View.VISIBLE);
        reviewPanel.removeAllViews();
        addReviewText("Submitted. Score: " + correctCount + "/" + gradableCount);
        addReviewAnswers(answerPayload);
    }

    private void addReviewAnswers(List<Map<String, Object>> answerPayload) {
        if (answerPayload.isEmpty()) {
            addReviewText("No answered questions.");
            return;
        }
        for (int i = 0; i < answerPayload.size(); i++) {
            Map<String, Object> answer = answerPayload.get(i);
            addReviewText((i + 1) + ". " + htmlToPlainText((String) answer.get("promptHtml")));
            addReviewText("Your answer: " + safeString(answer.get("displayAnswer")));
            addReviewText("Correct answer: " + safeString(answer.get("correctAnswer")));
        }
    }

    private void loadPastSubmissions() {
        MyApp app = (MyApp) getApplicationContext();
        String classroomId = app.getCurrentClassCode();
        String sectionId = app.getCurrentSectionId();
        String admissionNo = app.getCurrentStudentAdmissionNo();
        if (TextUtils.isEmpty(classroomId) || TextUtils.isEmpty(sectionId) || TextUtils.isEmpty(admissionNo)) {
            Toast.makeText(this, "Student verification is missing", Toast.LENGTH_SHORT).show();
            return;
        }

        setupPanel.setVisibility(View.GONE);
        questionPanel.setVisibility(View.GONE);
        reviewPanel.setVisibility(View.VISIBLE);
        reviewPanel.removeAllViews();
        addReviewText("Loading submissions...");

        FirebaseFirestore.getInstance()
                .collection(SUBMISSIONS_COLLECTION)
                .whereEqualTo("classroomId", classroomId)
                .whereEqualTo("studentKey", getStudentKey(sectionId, admissionNo))
                .get()
                .addOnSuccessListener(this::showPastSubmissions)
                .addOnFailureListener(e -> {
                    reviewPanel.removeAllViews();
                    addReviewText("Unable to load submissions.");
                });
    }

    private void showPastSubmissions(QuerySnapshot querySnapshot) {
        ArrayList<DocumentSnapshot> submissions = new ArrayList<>(querySnapshot.getDocuments());
        Collections.sort(submissions, (left, right) -> Long.compare(getSubmittedAtMillis(right), getSubmittedAtMillis(left)));

        reviewPanel.removeAllViews();
        if (submissions.isEmpty()) {
            addReviewText("No past submissions found.");
            return;
        }

        for (DocumentSnapshot submission : submissions) {
            Button button = new Button(this);
            button.setAllCaps(false);
            button.setText(getSubmissionTitle(submission));
            button.setOnClickListener(v -> showPastSubmissionReview(submission));
            reviewPanel.addView(button);
        }
    }

    private void showPastSubmissionReview(DocumentSnapshot submission) {
        reviewPanel.removeAllViews();
        addReviewText(getSubmissionTitle(submission));
        List<Map<String, Object>> answerPayload = (List<Map<String, Object>>) submission.get("answers");
        if (answerPayload == null) {
            addReviewText("No answers saved in this submission.");
        } else {
            addReviewAnswers(answerPayload);
        }
    }

    private String getSubmissionTitle(DocumentSnapshot submission) {
        String subject = safeString(submission.getString("subject"));
        String difficulty = safeString(submission.getString("difficulty"));
        Long correctCount = submission.getLong("correctCount");
        Long gradableCount = submission.getLong("gradableCount");
        return subject + " " + difficulty + " - " + safeLong(correctCount) + "/" + safeLong(gradableCount)
                + " - " + getSubmittedAtMillis(submission);
    }

    private String safeString(Object value) {
        return value == null ? "" : value.toString();
    }

    private long safeLong(Long value) {
        return value == null ? 0 : value;
    }

    private long getSubmittedAtMillis(DocumentSnapshot submission) {
        Long value = submission.getLong("submittedAtMillis");
        return value == null ? 0 : value;
    }

    private String getStudentKey(String sectionId, String admissionNo) {
        return sectionId + "_" + admissionNo;
    }

    private String getSubmissionDocumentId(String classroomId, String admissionNo) {
        return sanitizeDocumentId(classroomId) + "_" + sanitizeDocumentId(admissionNo);
    }

    private String sanitizeDocumentId(String value) {
        return value.replaceAll("[^A-Za-z0-9_-]", "_");
    }

    private void addReviewText(String value) {
        TextView textView = new TextView(this);
        textView.setText(value);
        textView.setTextColor(getResources().getColor(R.color.black));
        textView.setTextSize(16);
        textView.setPadding(0, 8, 0, 8);
        reviewPanel.addView(textView);
    }

    private Spanned fromHtml(String html) {
        String value = html == null ? "" : html;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(value, Html.FROM_HTML_MODE_LEGACY);
        }
        return Html.fromHtml(value);
    }

    private String htmlToPlainText(String html) {
        return fromHtml(html).toString().trim();
    }

    private static class Question {
        String id;
        String type;
        String className;
        String subject;
        String chapter;
        String difficulty;
        String promptHtml;
        String shortAnswerHtml;
        boolean trueAnswer;
        List<QuestionOption> options = new ArrayList<>();
        List<FibBank> fibBanks = new ArrayList<>();

        static Question fromDocument(DocumentSnapshot document) {
            Question question = new Question();
            question.id = document.getId();
            question.type = document.getString("type");
            question.className = document.getString("className");
            question.subject = document.getString("subject");
            question.chapter = document.getString("chapter");
            question.difficulty = document.getString("difficulty");
            question.promptHtml = document.getString("promptHtml");
            question.shortAnswerHtml = document.getString("shortAnswerHtml");
            Boolean trueAnswer = document.getBoolean("trueAnswer");
            question.trueAnswer = trueAnswer != null && trueAnswer;

            List<Map<String, Object>> rawOptions = (List<Map<String, Object>>) document.get("options");
            if (rawOptions != null) {
                for (Map<String, Object> rawOption : rawOptions) {
                    QuestionOption option = new QuestionOption();
                    Object html = rawOption.get("html");
                    Object correct = rawOption.get("correct");
                    option.html = html == null ? "" : html.toString();
                    option.correct = correct instanceof Boolean && (Boolean) correct;
                    question.options.add(option);
                }
            }

            List<Map<String, Object>> rawFibBanks = (List<Map<String, Object>>) document.get("fibBanks");
            if (rawFibBanks != null) {
                for (Map<String, Object> rawBank : rawFibBanks) {
                    FibBank bank = new FibBank();
                    Object label = rawBank.get("label");
                    bank.label = label == null ? "Blank" : label.toString();
                    List<Object> rawAnswers = (List<Object>) rawBank.get("answers");
                    if (rawAnswers != null) {
                        for (Object answer : rawAnswers) {
                            bank.answers.add(answer == null ? "" : answer.toString());
                        }
                    }
                    question.fibBanks.add(bank);
                }
            }

            if (TextUtils.isEmpty(question.type)
                    || TextUtils.isEmpty(question.className)
                    || TextUtils.isEmpty(question.subject)
                    || TextUtils.isEmpty(question.chapter)
                    || TextUtils.isEmpty(question.difficulty)) {
                return null;
            }
            return question;
        }
    }

    private static class QuestionOption {
        String html;
        boolean correct;
    }

    private static class FibBank {
        String label;
        List<String> answers = new ArrayList<>();
    }

    private static class Answer {
        String questionId;
        String type;
        Set<Integer> selectedOptions = new HashSet<>();
        Boolean trueFalseAnswer;
        List<String> fibAnswers = new ArrayList<>();
        String shortAnswer = "";
        String displayAnswer = "";

        Answer(String questionId, String type) {
            this.questionId = questionId;
            this.type = type;
        }

        boolean isEmpty() {
            if ("mcq".equals(type)) {
                return selectedOptions.isEmpty();
            }
            if ("true_false".equals(type)) {
                return trueFalseAnswer == null;
            }
            if ("fib".equals(type)) {
                for (String answer : fibAnswers) {
                    if (!TextUtils.isEmpty(answer)) {
                        return false;
                    }
                }
                return true;
            }
            return TextUtils.isEmpty(shortAnswer);
        }
    }
}
