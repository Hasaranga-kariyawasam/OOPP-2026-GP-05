package com.faculty.model.to;

/** Present / absent for one session (matches attendance.status in schema.sql). */
public enum AttendanceStatus {
    PRESENT("Present"),
    ABSENT("Absent");

    private final String label;

    AttendanceStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}
