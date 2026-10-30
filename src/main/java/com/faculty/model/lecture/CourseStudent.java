package com.faculty.model.lecture;

/** An undergraduate enrolled in one of the lecturer's courses (used in drop-downs). */
public class CourseStudent {

    private int stuId;
    private String regNumber;
    private String fullName;

    public CourseStudent(int stuId, String regNumber, String fullName) {
        this.stuId = stuId;
        this.regNumber = regNumber;
        this.fullName = fullName;
    }

    public int getStuId() { return stuId; }
    public String getRegNumber() { return regNumber; }
    public String getFullName() { return fullName; }

    @Override
    public String toString() {
        return regNumber + " - " + fullName;
    }
}
