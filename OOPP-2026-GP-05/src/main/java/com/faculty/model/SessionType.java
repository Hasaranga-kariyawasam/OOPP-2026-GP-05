package com.faculty.model;

/** Kind of teaching session (matches the session_type ENUM in schema.sql). */
public enum SessionType {
    THEORY("Theory"),
    PRACTICAL("Practical");

    private final String label;

    SessionType(String label) {
        this.label = label;
    }

    /** Text shown in the GUI. */
    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}
