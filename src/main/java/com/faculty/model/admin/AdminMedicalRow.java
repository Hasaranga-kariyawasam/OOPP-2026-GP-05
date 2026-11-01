package com.faculty.model.admin;

import java.time.LocalDate;

/** A medical request shown in the admin medical screen. */
public record AdminMedicalRow(int medicalId, String regNumber, String studentName, LocalDate sessionDate,
                              String status, String reason) {
}
