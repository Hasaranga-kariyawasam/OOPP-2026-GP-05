package com.faculty.model.admin;

/** One mark of one student in one course. */
public record AdminMark(int markId, int courseId, String regNumber, String courseCode,
                        String evaluationType, String category, double value) {
}
