package com.faculty.dao;

import com.faculty.model.lecture.GradeLine;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

/** ABSTRACTION: the grade table (final mark, grade, grade point of each student in each course). */
public interface LecturerGradeDAO {

    /** Every recorded grade of every undergraduate, ordered by registration number. */
    List<GradeLine> findAllLines() throws SQLException;

    /** Semester of the course (stored with every grade row), or null when the course does not exist. */
    String findCourseSemester(int courseId) throws SQLException;

    /** Inserts the grade, or updates it when the student already has one for this course. */
    void saveGrade(int stuId, int courseId, BigDecimal finalMark, String grade, BigDecimal gradePoint,
                   String semester) throws SQLException;

    void deleteGrade(int stuId, int courseId) throws SQLException;
}
