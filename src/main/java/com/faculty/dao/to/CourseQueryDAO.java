package com.faculty.dao;

import com.faculty.model.Course;

import java.sql.SQLException;
import java.util.List;

/** Read-only course lookups for the Technical Officer screens (course create / edit belongs to the Admin). */
public interface CourseQueryDAO {

    /** Every course, ordered by course code. */
    List<Course> findAll() throws SQLException;
}
