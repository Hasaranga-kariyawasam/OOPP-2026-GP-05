package com.faculty.dao.lecturer;

import com.faculty.config.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LecturerDAOImpl implements LecturerDAO {

    // SEC-04: every query uses a '?' placeholder, never string concatenation.
    private static final String COUNT_COURSES =
        "SELECT COUNT(*) FROM course WHERE lecture_id = ?";

    private static final String COUNT_STUDENTS =
        "SELECT COUNT(DISTINCT e.stu_id) FROM enrollment e "
      + "JOIN course c ON c.course_id = e.course_id WHERE c.lecture_id = ?";

    private static final String COUNT_MATERIALS =
        "SELECT COUNT(*) FROM course_material WHERE lecture_id = ?";

    @Override
    public int countCourses(int lecturerId) throws SQLException {
        return count(COUNT_COURSES, lecturerId);
    }

    @Override
    public int countStudents(int lecturerId) throws SQLException {
        return count(COUNT_STUDENTS, lecturerId);
    }

    @Override
    public int countMaterials(int lecturerId) throws SQLException {
        return count(COUNT_MATERIALS, lecturerId);
    }

    private int count(String sql, int lecturerId) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, lecturerId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }
}
