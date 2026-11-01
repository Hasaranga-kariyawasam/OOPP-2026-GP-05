package com.faculty.model.lecture;

import java.util.List;

/** SGPA (latest semester) and CGPA of one student, with the subjects they come from (FR-23). */
public class GpaSummaryRow {

    private final int stuId;
    private final String regNumber;
    private final String studentName;
    private final String status;
    private final int totalCredits;
    private final String latestSemester;
    private final double sgpa;
    private final double cgpa;
    private final List<GradeLine> lines;

    public GpaSummaryRow(int stuId, String regNumber, String studentName, String status, int totalCredits,
                         String latestSemester, double sgpa, double cgpa, List<GradeLine> lines) {
        this.stuId = stuId;
        this.regNumber = regNumber;
        this.studentName = studentName;
        this.status = status;
        this.totalCredits = totalCredits;
        this.latestSemester = latestSemester;
        this.sgpa = sgpa;
        this.cgpa = cgpa;
        this.lines = lines;
    }

    public int getStuId() { return stuId; }
    public String getRegNumber() { return regNumber; }
    public String getStudentName() { return studentName; }
    public String getStatus() { return status; }
    public int getTotalCredits() { return totalCredits; }
    public String getLatestSemester() { return latestSemester; }
    public double getSgpa() { return sgpa; }
    public double getCgpa() { return cgpa; }
    public List<GradeLine> getLines() { return lines; }
}
