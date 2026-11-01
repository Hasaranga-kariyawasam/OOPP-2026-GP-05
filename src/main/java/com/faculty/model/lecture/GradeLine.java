package com.faculty.model.lecture;

import java.math.BigDecimal;

/** One recorded grade of a student (a row of the grade table with the student and course details). */
public class GradeLine {

    private int stuId;
    private String regNumber;
    private String studentName;
    private String status;          // REGULAR, REPEAT, BATCH_MISSED
    private String courseCode;
    private String courseName;
    private int credits;            // theory + practical credits
    private String semester;
    private BigDecimal finalMark;
    private String grade;
    private BigDecimal gradePoint;

    public int getStuId() { return stuId; }
    public void setStuId(int stuId) { this.stuId = stuId; }
    public String getRegNumber() { return regNumber; }
    public void setRegNumber(String regNumber) { this.regNumber = regNumber; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }
    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    public int getCredits() { return credits; }
    public void setCredits(int credits) { this.credits = credits; }
    public String getSemester() { return semester; }
    public void setSemester(String semester) { this.semester = semester; }
    public BigDecimal getFinalMark() { return finalMark; }
    public void setFinalMark(BigDecimal finalMark) { this.finalMark = finalMark; }
    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }
    public BigDecimal getGradePoint() { return gradePoint; }
    public void setGradePoint(BigDecimal gradePoint) { this.gradePoint = gradePoint; }
}
