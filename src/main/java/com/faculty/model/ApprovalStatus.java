package com.faculty.model;

/** Decision on a medical submission (matches medical_record.approval_status in schema.sql). */
public enum ApprovalStatus {
    PENDING("Pending"),
    APPROVED("Approved"),
    REJECTED("Not Approved");

    private final String label;

    ApprovalStatus(String label) {
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
