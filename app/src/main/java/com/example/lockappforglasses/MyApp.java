package com.example.lockappforglasses;

import android.app.Application;

import java.util.HashMap;
import java.util.Map;

public class MyApp extends Application {

    private String currentClassCode = null;
    private String currentSectionId = null;
    private String currentStudentAdmissionNo = null;
    private String currentStudentName = null;
    private boolean currentQuizModeEnabled = false;
    private String currentQuestionBankListId = null;
    private Map<String, Integer> currentRandomQuestionTypeCounts = new HashMap<>();
    private Map<String, String> currentStudentDifficultyLevels = new HashMap<>();

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public void setCurrentClassCode(String value) {
        this.currentClassCode = value;
    }

    public String getCurrentClassCode() {
        return currentClassCode;
    }

    public void setCurrentStudent(String sectionId, String admissionNo, String name) {
        this.currentSectionId = sectionId;
        this.currentStudentAdmissionNo = admissionNo;
        this.currentStudentName = name;
    }

    public String getCurrentSectionId() {
        return currentSectionId;
    }

    public String getCurrentStudentAdmissionNo() {
        return currentStudentAdmissionNo;
    }

    public String getCurrentStudentName() {
        return currentStudentName;
    }

    public void setCurrentQuizModeEnabled(boolean value) {
        this.currentQuizModeEnabled = value;
    }

    public boolean isCurrentQuizModeEnabled() {
        return currentQuizModeEnabled;
    }

    public void setCurrentQuestionBankListId(String value) {
        this.currentQuestionBankListId = value;
    }

    public String getCurrentQuestionBankListId() {
        return currentQuestionBankListId;
    }

    public void setCurrentQuizSessionOptions(
            Map<String, Integer> randomQuestionTypeCounts,
            Map<String, String> studentDifficultyLevels) {
        this.currentRandomQuestionTypeCounts = randomQuestionTypeCounts == null
                ? new HashMap<>()
                : new HashMap<>(randomQuestionTypeCounts);
        this.currentStudentDifficultyLevels = studentDifficultyLevels == null
                ? new HashMap<>()
                : new HashMap<>(studentDifficultyLevels);
    }

    public Map<String, Integer> getCurrentRandomQuestionTypeCounts() {
        return new HashMap<>(currentRandomQuestionTypeCounts);
    }

    public String getCurrentStudentDifficultyLevel() {
        if (currentStudentAdmissionNo == null) {
            return "";
        }
        String difficulty = currentStudentDifficultyLevels.get(currentStudentAdmissionNo);
        return difficulty == null ? "" : difficulty;
    }

    public void clearTempString() {
        this.currentClassCode = null;
        this.currentSectionId = null;
        this.currentStudentAdmissionNo = null;
        this.currentStudentName = null;
        this.currentQuizModeEnabled = false;
        this.currentQuestionBankListId = null;
        this.currentRandomQuestionTypeCounts.clear();
        this.currentStudentDifficultyLevels.clear();
    }
}
