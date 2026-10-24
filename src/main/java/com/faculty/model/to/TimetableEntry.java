package com.faculty.model;

import java.time.LocalTime;

/** One scheduled session in the weekly timetable (row of the timetable table + course info). */
public class TimetableEntry {

    private int timetableId;
    private int courseId;
    private String courseCode;
    private String courseName;
    private String lecturerName;
    private String day;                 // MON .. SUN
    private LocalTime startTime;
    private LocalTime endTime;
    private SessionType sessionType;
    private String venue;

    public int getTimetableId() { return timetableId; }
    public void setTimetableId(int timetableId) { this.timetableId = timetableId; }
    public int getCourseId() { return courseId; }
    public void setCourseId(int courseId) { this.courseId = courseId; }
    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }
    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    public String getLecturerName() { return lecturerName; }
    public void setLecturerName(String lecturerName) { this.lecturerName = lecturerName; }
    public String getDay() { return day; }
    public void setDay(String day) { this.day = day; }
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
    public SessionType getSessionType() { return sessionType; }
    public void setSessionType(SessionType sessionType) { this.sessionType = sessionType; }
    public String getVenue() { return venue; }
    public void setVenue(String venue) { this.venue = venue; }

    /** "09:00 - 11:00" */
    public String getTimeRange() {
        return startTime.toString().substring(0, 5) + " - " + endTime.toString().substring(0, 5);
    }
}
