package com.faculty.dao;

import com.faculty.model.lecture.CourseStudent;
import com.faculty.model.lecture.MarkEntry;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

/** ABSTRACTION: marks queries for the lecturer module. */
public interface LecturerMarksDAO {

    /** Students enrolled in the course (any academic year), ordered by reg. number. */
    List<CourseStudent> findEnrolledStudents(int courseId) throws SQLException;

    /** Every mark stored for the course. */
    List<MarkEntry> findByCourse(int courseId) throws SQLException;

    boolean isEnrolled(int stuId, int courseId) throws SQLException;

    /** mark_id of an existing (student, course, evaluation type) mark, or -1 when there is none. */
    int findMarkId(int stuId, int courseId, String evaluationType) throws SQLException;

    void insert(int stuId, int courseId, String evaluationType, String category, BigDecimal value) throws SQLException;

    /** Only changes the row when the course really belongs to this lecturer. Returns rows changed. */
    int update(int markId, int lecturerId, String category, BigDecimal value) throws SQLException;

    /** Only deletes the row when the course really belongs to this lecturer. Returns rows deleted. */
    int delete(int markId, int lecturerId) throws SQLException;
}
