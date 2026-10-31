package com.faculty.model.admin;

/** A lecturer or technical officer. designation is only used for lecturers. userId 0 = not saved yet. */
public record AdminStaff(int userId, String username, String firstName, String lastName, String email,
                         int depId, String departmentName, String designation) {
    public String fullName() {
        return firstName + " " + lastName;
    }
}
