package com.faculty.model.admin;

/** A course row. courseId 0 = not saved yet; lecturerId null = no lecturer assigned. */
public record AdminCourse(int courseId, String code, String name, int theoryCredits, int practicalCredits,
                          String semester, String type, Integer lecturerId, String lecturerName) {
}
