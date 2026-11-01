package com.faculty.model.admin;

import java.time.LocalDate;

/** The profile fields an admin may change about himself. */
public record AdminProfileData(String firstName, String lastName, String email, String phone, String nic,
                               LocalDate dob, String street, String city, String postalCode, String country,
                               String picturePath) {
}
