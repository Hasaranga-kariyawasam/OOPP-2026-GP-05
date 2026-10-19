package com.faculty.model;

/** One student's attendance in one course, split into theory / practical / combined (FR-30). */
public class StudentCourseAttendance {

    private final Course course;
    private final AttendanceSummary theory;
    private final AttendanceSummary practical;
    private final AttendanceSummary combined;

    public StudentCourseAttendance(Course course, AttendanceSummary theory,
                                   AttendanceSummary practical, AttendanceSummary combined) {
        this.course = course;
        this.theory = theory;
        this.practical = practical;
        this.combined = combined;
    }

    public Course getCourse() { return course; }
    public AttendanceSummary getTheory() { return theory; }
    public AttendanceSummary getPractical() { return practical; }
    public AttendanceSummary getCombined() { return combined; }
}
