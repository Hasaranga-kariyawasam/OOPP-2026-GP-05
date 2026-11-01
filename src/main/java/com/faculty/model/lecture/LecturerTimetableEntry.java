package com.faculty.model.lecture;

import java.time.LocalTime;

/** One weekly slot of the lecturer's timetable. */
public class LecturerTimetableEntry {

    private String day;            // MON ... SUN
    private LocalTime startTime;
    private LocalTime endTime;
    private String courseCode;
    private String courseName;
    private String sessionType;    // THEORY or PRACTICAL
    private String venue;

    public String getDay() { return day; }
    public void setDay(String day) { this.day = day; }
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }
    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    public String getSessionType() { return sessionType; }
    public void setSessionType(String sessionType) { this.sessionType = sessionType; }
    public String getVenue() { return venue; }
    public void setVenue(String venue) { this.venue = venue; }
}
