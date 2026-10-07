package com.faculty.controller.admin;

/** Manage Technical Officers (admin section "tech_officers"). All logic lives in StaffSectionBase. */
public class TechOfficersSectionController extends StaffSectionBase {
    @Override protected String role() { return "TECHNICAL_OFFICER"; }
    @Override protected String label() { return "Technical Officer"; }
}
