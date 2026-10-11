package com.faculty.dao.lecturer;

import java.sql.SQLException;

/** ABSTRACTION: lecturer-related database queries. The service layer uses this, not JDBC. */
public interface LecturerDAO {

    /** Number of courses assigned to this lecturer (course.lecture_id). */
    int countCourses(int lecturerId) throws SQLException;

    /** Number of different students enrolled in this lecturer's courses. */
    int countStudents(int lecturerId) throws SQLException;

    /** Number of course materials uploaded by this lecturer. */
    int countMaterials(int lecturerId) throws SQLException;
}
