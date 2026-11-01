package com.faculty.dao;

import java.sql.SQLException;
import java.time.LocalDate;

/** ABSTRACTION: a lecturer updating his own profile (everything except username and password). */
public interface LecturerProfileDAO {

    void updateProfile(int userId, String firstName, String lastName, String email, String nic,
                       LocalDate dob, String phone, String street, String city,
                       String postalCode, String country, String picturePath) throws SQLException;
}
