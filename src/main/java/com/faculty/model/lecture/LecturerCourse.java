package com.faculty.model.lecture;

/**
 * A course assigned to a lecturer (course table + number of enrolled students).
 * ENCAPSULATION: private fields, access through getters/setters.
 */
public class LecturerCourse {

    private int courseId;
    private String courseCode;
    private String courseName;
    private int creditsTheory;
    private int creditsPractical;
    private String semester;
    private String courseType;      // THEORY, PRACTICAL, BOTH
    private int studentCount;

    public int getTotalCredits() { return creditsTheory + creditsPractical; }

    /** Friendly text for the course_type column. */
    public String getTypeLabel() {
        if (courseType == null) return "-";
        return switch (courseType) {
            case "THEORY" -> "Theory";
            case "PRACTICAL" -> "Practical";
            case "BOTH" -> "Theory + Practical";
            default -> courseType;
        };
    }

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
    public int getStudentCount() { return studentCount; }
    public void setStudentCount(int studentCount) { this.studentCount = studentCount; }
}
