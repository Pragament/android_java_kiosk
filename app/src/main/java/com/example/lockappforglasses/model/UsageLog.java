package com.example.lockappforglasses.model;

public class UsageLog {
    private String studentName;
    private String studentLog;
    private long timestamp;
    private String packageName;
    private String appName;

    public UsageLog() {}

    public UsageLog(String studentName, String studentLog, long timestamp, String packageName, String appName) {
        this.studentName = studentName;
        this.studentLog = studentLog;
        this.timestamp = timestamp;
        this.packageName = packageName;
        this.appName = appName;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStudentLog() {
        return studentLog;
    }

    public void setStudentLog(String studentLog) {
        this.studentLog = studentLog;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }
}
