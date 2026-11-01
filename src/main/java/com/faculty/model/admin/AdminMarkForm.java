package com.faculty.model.admin;

/** What the admin typed on the marks screen. courseId 0 = nothing chosen. */
public record AdminMarkForm(String regNumber, int courseId, String evaluationType, String category, String valueText) {
}
