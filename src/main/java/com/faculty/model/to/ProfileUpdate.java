package com.faculty.model;

import java.time.LocalDate;

/**
 * The profile fields a user is allowed to change (FR-13): everything except
 * username and password. A Java record = an immutable data carrier.
 */
public record ProfileUpdate(String firstName,
                            String lastName,
                            String email,
                            String nic,
                            LocalDate dob,
                            String phone,
                            String street,
                            String city,
                            String postalCode,
                            String country) {
}
