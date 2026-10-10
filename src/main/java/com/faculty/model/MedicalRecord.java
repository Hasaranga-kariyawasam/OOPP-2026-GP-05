package com.faculty.model;

import java.time.LocalDate;

/**
 * A medical submission for one absence date (row of the medical_record table).
 * Student name / reg number are filled in by the DAO's JOIN so the table can show them.
 */
public class MedicalRecord {

    private int medicalId;
    private int stuId;
    private String regNumber;
    private String studentName;
    private String documentPath;
    private LocalDate sessionDate;
    private String reason;
    private ApprovalStatus approvalStatus = ApprovalStatus.PENDING;
    private Integer approvedBy;
    private String approvedByName;

    public int getMedicalId() { return medicalId; }
    public void setMedicalId(int medicalId) { this.medicalId = medicalId; }
    public int getStuId() { return stuId; }
    public void setStuId(int stuId) { this.stuId = stuId; }
    public String getRegNumber() { return regNumber; }
    public void setRegNumber(String regNumber) { this.regNumber = regNumber; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public String getDocumentPath() { return documentPath; }
    public void setDocumentPath(String documentPath) { this.documentPath = documentPath; }
    public LocalDate getSessionDate() { return sessionDate; }
    public void setSessionDate(LocalDate sessionDate) { this.sessionDate = sessionDate; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public ApprovalStatus getApprovalStatus() { return approvalStatus; }
    public void setApprovalStatus(ApprovalStatus approvalStatus) { this.approvalStatus = approvalStatus; }
    public Integer getApprovedBy() { return approvedBy; }
    public void setApprovedBy(Integer approvedBy) { this.approvedBy = approvedBy; }
    public String getApprovedByName() { return approvedByName; }
    public void setApprovedByName(String approvedByName) { this.approvedByName = approvedByName; }

    /** Details can only be edited while the medical has not been decided yet (FR-37). */
    public boolean isEditable() {
        return approvalStatus == ApprovalStatus.PENDING;
    }
}
