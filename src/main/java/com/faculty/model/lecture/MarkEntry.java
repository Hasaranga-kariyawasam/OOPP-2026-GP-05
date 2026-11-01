package com.faculty.model.lecture;

import java.math.BigDecimal;

/** One row of the marks table (a single evaluation mark of one student in one course). */
public class MarkEntry {

    private int markId;
    private int stuId;
    private int courseId;
    private String regNumber;
    private String studentName;
    private String evaluationType;   // QUIZ1, ASSIGNMENT, MID, FINAL ...
    private String category;         // CA or FINAL
    private BigDecimal marksValue;   // out of 100

    public int getMarkId() { return markId; }
    public void setMarkId(int markId) { this.markId = markId; }
    public int getStuId() { return stuId; }
    public void setStuId(int stuId) { this.stuId = stuId; }
    public int getCourseId() { return courseId; }
    public void setCourseId(int courseId) { this.courseId = courseId; }
    public String getRegNumber() { return regNumber; }
    public void setRegNumber(String regNumber) { this.regNumber = regNumber; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public String getEvaluationType() { return evaluationType; }
    public void setEvaluationType(String evaluationType) { this.evaluationType = evaluationType; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public BigDecimal getMarksValue() { return marksValue; }
    public void setMarksValue(BigDecimal marksValue) { this.marksValue = marksValue; }
}
