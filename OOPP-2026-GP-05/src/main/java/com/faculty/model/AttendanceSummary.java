package com.faculty.model;

import com.faculty.service.AttendanceCalculator;

/**
 * Attendance totals of ONE undergraduate in ONE course for one scope
 * (theory only, practical only, or combined) - one row of the summary tables.
 * Percentages are calculated from the hours, using AttendanceCalculator.
 */
public class AttendanceSummary {

    private int stuId;
    private String regNumber;
    private String studentName;
    private String batch;
    private String studentStatus;       // REGULAR, REPEAT, BATCH_MISSED
    private int courseId;
    private String courseCode;
    private String courseName;

    private double presentHours;
    private double totalHours;
    private double excusedHours;        // absent hours covered by an APPROVED medical
    private int presentSessions;
    private int totalSessions;
    private int medicalSessions;        // absences that have ANY medical (approved or not)

    public int getStuId() { return stuId; }
    public void setStuId(int stuId) { this.stuId = stuId; }
    public String getRegNumber() { return regNumber; }
    public void setRegNumber(String regNumber) { this.regNumber = regNumber; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public String getBatch() { return batch; }
    public void setBatch(String batch) { this.batch = batch; }
    public String getStudentStatus() { return studentStatus; }
    public void setStudentStatus(String studentStatus) { this.studentStatus = studentStatus; }
    public int getCourseId() { return courseId; }
    public void setCourseId(int courseId) { this.courseId = courseId; }
    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }
    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    public double getPresentHours() { return presentHours; }
    public void setPresentHours(double presentHours) { this.presentHours = presentHours; }
    public double getTotalHours() { return totalHours; }
    public void setTotalHours(double totalHours) { this.totalHours = totalHours; }
    public double getExcusedHours() { return excusedHours; }
    public void setExcusedHours(double excusedHours) { this.excusedHours = excusedHours; }
    public int getPresentSessions() { return presentSessions; }
    public void setPresentSessions(int presentSessions) { this.presentSessions = presentSessions; }
    public int getTotalSessions() { return totalSessions; }
    public void setTotalSessions(int totalSessions) { this.totalSessions = totalSessions; }
    public int getMedicalSessions() { return medicalSessions; }
    public void setMedicalSessions(int medicalSessions) { this.medicalSessions = medicalSessions; }

    /** Attendance % counting only the sessions the student really attended. */
    public double getRawPercentage() {
        return AttendanceCalculator.percentage(presentHours, totalHours);
    }

    /** Attendance % when absences covered by an APPROVED medical are not counted as missed (FR-35). */
    public double getAdjustedPercentage() {
        return AttendanceCalculator.percentage(presentHours + excusedHours, totalHours);
    }

    public AttendanceCalculator.Scenario getScenario() {
        return AttendanceCalculator.scenario(getRawPercentage(), medicalSessions > 0);
    }

    /** Attendance part of the eligibility rule (FR-48): adjusted percentage must be at least 80%. */
    public boolean isEligible() {
        return totalSessions > 0 && AttendanceCalculator.meetsRequirement(getAdjustedPercentage());
    }

    /** Adds another scope's totals into this one (used to build the combined view). */
    public void add(AttendanceSummary other) {
        presentHours += other.presentHours;
        totalHours += other.totalHours;
        excusedHours += other.excusedHours;
        presentSessions += other.presentSessions;
        totalSessions += other.totalSessions;
        medicalSessions += other.medicalSessions;
    }
}
