package com.faculty.dao;

import com.faculty.model.lecture.LecturerCourse;

import java.sql.SQLException;
import java.util.List;

/** ABSTRACTION: course queries for the lecturer module. */
public interface LecturerCourseDAO {

    /** All courses whose course.lecture_id is this lecturer, ordered by semester then code. */
    List<LecturerCourse> findByLecturer(int lecturerId) throws SQLException;
}
