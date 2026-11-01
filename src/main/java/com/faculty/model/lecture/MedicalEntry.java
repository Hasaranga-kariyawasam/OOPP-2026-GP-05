package com.faculty.model.lecture;

import java.time.LocalDate;

/** A medical record of a student in one of the lecturer's courses (view-only for lecturers). */
public class MedicalEntry {

    private int medicalId;
    private String regNumber;
    private String studentName;
    private LocalDate sessionDate;
    private String reason;
    private String approvalStatus;   // PENDING, APPROVED, REJECTED
    private String decidedBy;        // technical officer name, null while pending

    public int getMedicalId() { return medicalId; }
    public void setMedicalId(int medicalId) { this.medicalId = medicalId; }
    public String getRegNumber() { return regNumber; }
    public void setRegNumber(String regNumber) { this.regNumber = regNumber; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public LocalDate getSessionDate() { return sessionDate; }
    public void setSessionDate(LocalDate sessionDate) { this.sessionDate = sessionDate; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getApprovalStatus() { return approvalStatus; }
    public void setApprovalStatus(String approvalStatus) { this.approvalStatus = approvalStatus; }
    public String getDecidedBy() { return decidedBy; }
    public void setDecidedBy(String decidedBy) { this.decidedBy = decidedBy; }
}
