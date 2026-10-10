package com.faculty.model;

/**
 * A course unit (row of the course table).
 * ENCAPSULATION: private fields + getters. Only the fields the Technical Officer screens need.
 */
public class Course {

    private int courseId;
    private String courseCode;
    private String courseName;
    private int creditsTheory;
    private int creditsPractical;
    private String semester;
    private String courseType;      // THEORY, PRACTICAL or BOTH

    public int getCourseId() { return courseId; }
    public void setCourseId(int courseId) { this.courseId = courseId; }
    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }
    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    public int getCreditsTheory() { return creditsTheory; }
    public void setCreditsTheory(int creditsTheory) { this.creditsTheory = creditsTheory; }
    public int getCreditsPractical() { return creditsPractical; }
    public void setCreditsPractical(int creditsPractical) { this.creditsPractical = creditsPractical; }
    public String getSemester() { return semester; }
    public void setSemester(String semester) { this.semester = semester; }
    public String getCourseType() { return courseType; }
    public void setCourseType(String courseType) { this.courseType = courseType; }

    /** True when the course has a theory component (THEORY or BOTH). */
    public boolean hasTheory() {
        return "THEORY".equals(courseType) || "BOTH".equals(courseType);
    }

    /** True when the course has a practical component (PRACTICAL or BOTH). */
    public boolean hasPractical() {
        return "PRACTICAL".equals(courseType) || "BOTH".equals(courseType);
    }

    /** True when attendance can be recorded for the given session type. */
    public boolean supports(SessionType type) {
        return type == SessionType.THEORY ? hasTheory() : hasPractical();
    }

    @Override
    public String toString() {
        return courseCode + " - " + courseName;
    }
}
