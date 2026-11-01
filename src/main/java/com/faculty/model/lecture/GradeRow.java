package com.faculty.model.lecture;

/**
 * Final mark and grade of one student in one course. Null values mean "not available yet"
 * (no CA marks, or the final exam mark has not been entered).
 */
public class GradeRow {

    private final String regNumber;
    private final String studentName;
    private final Double caAverage;
    private final Double finalExam;
    private final Double finalMark;
    private final String grade;
    private final Double gradePoint;

    public GradeRow(String regNumber, String studentName, Double caAverage, Double finalExam,
                    Double finalMark, String grade, Double gradePoint) {
        this.regNumber = regNumber;
        this.studentName = studentName;
        this.caAverage = caAverage;
        this.finalExam = finalExam;
        this.finalMark = finalMark;
        this.grade = grade;
        this.gradePoint = gradePoint;
    }

    public String getRegNumber() { return regNumber; }
    public String getStudentName() { return studentName; }
    public Double getCaAverage() { return caAverage; }
    public Double getFinalExam() { return finalExam; }
    public Double getFinalMark() { return finalMark; }
    public String getGrade() { return grade; }
    public Double getGradePoint() { return gradePoint; }
    public boolean isGraded() { return finalMark != null; }
}
