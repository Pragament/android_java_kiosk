package com.example.lockappforglasses;

import android.app.Application;

public class MyApp extends Application {

    private String currentClassCode = null;
    private String currentSectionId = null;
    private String currentStudentAdmissionNo = null;
    private String currentStudentName = null;
    private boolean currentQuizModeEnabled = false;

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

    public void clearTempString() {
        this.currentClassCode = null;
        this.currentSectionId = null;
        this.currentStudentAdmissionNo = null;
        this.currentStudentName = null;
        this.currentQuizModeEnabled = false;
    }
}
