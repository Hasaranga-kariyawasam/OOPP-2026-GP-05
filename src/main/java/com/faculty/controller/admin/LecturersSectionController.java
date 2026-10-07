package com.faculty.controller.admin;

/** Manage Lecturers (admin section "lecturers"). All logic lives in StaffSectionBase. */
public class LecturersSectionController extends StaffSectionBase {
    @Override protected String role() { return "LECTURER"; }
    @Override protected String label() { return "Lecturer"; }
}
