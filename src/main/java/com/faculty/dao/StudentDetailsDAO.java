package com.faculty.dao;

import com.faculty.model.lecture.StudentDetails;

import java.sql.SQLException;
import java.util.List;

/** ABSTRACTION: read-only access to undergraduate details for the lecturer module. */
public interface StudentDetailsDAO {

    /** Every undergraduate, ordered by registration number. */
    List<StudentDetails> findAll() throws SQLException;
}
