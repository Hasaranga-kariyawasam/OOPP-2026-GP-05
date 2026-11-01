package com.faculty.model.admin;

/** What the admin typed on the course screen (still text, validated by AdminCourseService). */
public record AdminCourseForm(String code, String name, String theoryCredits, String practicalCredits,
                              String semester, String type, Integer lecturerId) {
}
