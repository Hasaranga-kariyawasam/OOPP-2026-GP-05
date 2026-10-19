package com.faculty.model;

/**
 * One line of the "mark attendance" sheet: an enrolled undergraduate plus the status
 * that is currently saved (or PRESENT by default) and any medical on that date.
 */
public class AttendanceSheetRow {

    private final int stuId;
    private final String regNumber;
    private final String studentName;
    private AttendanceStatus status = AttendanceStatus.PRESENT;
    private boolean alreadySaved;
    private ApprovalStatus medicalStatus;       // null = no medical on this date

    public AttendanceSheetRow(int stuId, String regNumber, String studentName) {
        this.stuId = stuId;
        this.regNumber = regNumber;
        this.studentName = studentName;
    }

    public int getStuId() { return stuId; }
    public String getRegNumber() { return regNumber; }
    public String getStudentName() { return studentName; }
    public AttendanceStatus getStatus() { return status; }
    public void setStatus(AttendanceStatus status) { this.status = status; }
    public boolean isAlreadySaved() { return alreadySaved; }
    public void setAlreadySaved(boolean alreadySaved) { this.alreadySaved = alreadySaved; }
    public ApprovalStatus getMedicalStatus() { return medicalStatus; }
    public void setMedicalStatus(ApprovalStatus medicalStatus) { this.medicalStatus = medicalStatus; }
}
