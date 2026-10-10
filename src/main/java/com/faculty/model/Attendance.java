package com.faculty.model;

import java.time.LocalDate;

/**
 * One undergraduate's attendance for one session of one course (row of the attendance table).
 * ENCAPSULATION: private fields, access through getters / setters.
 */
public class Attendance {

    private int attendanceId;
    private int stuId;
    private int courseId;
    private Integer toId;               // Technical Officer who recorded it
    private Integer medicalId;          // medical covering this absence (null = none)
    private int sessionNo;              // 1..15
    private SessionType sessionType;
    private LocalDate sessionDate;
    private AttendanceStatus status;
    private double sessionHours = 2.0;  // 1 session = 2 hours (project brief)
    private ApprovalStatus medicalStatus;   // status of the linked medical (null = no medical)

    public int getAttendanceId() { return attendanceId; }
    public void setAttendanceId(int attendanceId) { this.attendanceId = attendanceId; }
    public int getStuId() { return stuId; }
    public void setStuId(int stuId) { this.stuId = stuId; }
    public int getCourseId() { return courseId; }
    public void setCourseId(int courseId) { this.courseId = courseId; }
    public Integer getToId() { return toId; }
    public void setToId(Integer toId) { this.toId = toId; }
    public Integer getMedicalId() { return medicalId; }
    public void setMedicalId(Integer medicalId) { this.medicalId = medicalId; }
    public int getSessionNo() { return sessionNo; }
    public void setSessionNo(int sessionNo) { this.sessionNo = sessionNo; }
    public SessionType getSessionType() { return sessionType; }
    public void setSessionType(SessionType sessionType) { this.sessionType = sessionType; }
    public LocalDate getSessionDate() { return sessionDate; }
    public void setSessionDate(LocalDate sessionDate) { this.sessionDate = sessionDate; }
    public AttendanceStatus getStatus() { return status; }
    public void setStatus(AttendanceStatus status) { this.status = status; }
    public double getSessionHours() { return sessionHours; }
    public void setSessionHours(double sessionHours) { this.sessionHours = sessionHours; }

    public ApprovalStatus getMedicalStatus() { return medicalStatus; }
    public void setMedicalStatus(ApprovalStatus medicalStatus) { this.medicalStatus = medicalStatus; }

    public boolean isPresent() {
        return status == AttendanceStatus.PRESENT;
    }
}
