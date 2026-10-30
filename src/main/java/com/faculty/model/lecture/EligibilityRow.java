package com.faculty.model.lecture;

/** Exam eligibility of one student in one course (FR-20, FR-21): CA >= 40% and attendance >= 80%. */
public class EligibilityRow {

    private final String regNumber;
    private final String studentName;
    private final Double caAverage;        // null when the student has no CA marks yet
    private final double attendancePercent;
    private final boolean caOk;
    private final boolean attendanceOk;

    public EligibilityRow(String regNumber, String studentName, Double caAverage,
                          double attendancePercent, boolean caOk, boolean attendanceOk) {
        this.regNumber = regNumber;
        this.studentName = studentName;
        this.caAverage = caAverage;
        this.attendancePercent = attendancePercent;
        this.caOk = caOk;
        this.attendanceOk = attendanceOk;
    }

    public String getRegNumber() { return regNumber; }
    public String getStudentName() { return studentName; }
    public Double getCaAverage() { return caAverage; }
    public double getAttendancePercent() { return attendancePercent; }
    public boolean isCaOk() { return caOk; }
    public boolean isAttendanceOk() { return attendanceOk; }
    public boolean isEligible() { return caOk && attendanceOk; }

    /** Short explanation shown in the table. */
    public String getRemark() {
        if (isEligible()) return "Eligible";
        if (caAverage == null) return attendanceOk ? "No CA marks yet" : "No CA marks, attendance below 80%";
        if (!caOk && !attendanceOk) return "CA below 40% and attendance below 80%";
        return caOk ? "Attendance below 80%" : "CA below 40%";
    }
}
