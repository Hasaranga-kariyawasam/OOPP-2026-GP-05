package com.faculty.model.admin;

import java.time.LocalDate;

/** An undergraduate as the admin screens see it (users + undergraduate + department name). userId 0 = not saved yet. */
public record AdminStudent(int userId, String regNumber, String firstName, String lastName, String username,
                           String email, String batch, String status, LocalDate intakeDate,
                           int depId, String departmentName) {
    public String fullName() {
        return firstName + " " + lastName;
    }
}
