package com.faculty.dao;

import com.faculty.config.DBConnection;
import com.faculty.model.lecture.LecturerCourse;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LecturerCourseDAOImpl implements LecturerCourseDAO {

    // SEC-04: '?' placeholder. LEFT JOIN so a course with no enrolments still appears (count = 0).
    private static final String FIND_BY_LECTURER =
        "SELECT c.course_id, c.course_code, c.course_name, c.credits_theory, c.credits_practical, "
      + "       c.semester, c.course_type, COUNT(DISTINCT e.stu_id) AS student_count "
      + "FROM course c "
      + "LEFT JOIN enrollment e ON e.course_id = c.course_id "
      + "WHERE c.lecture_id = ? "
      + "GROUP BY c.course_id, c.course_code, c.course_name, c.credits_theory, "
      + "         c.credits_practical, c.semester, c.course_type "
      + "ORDER BY c.semester, c.course_code";

    @Override
    public List<LecturerCourse> findByLecturer(int lecturerId) throws SQLException {
        List<LecturerCourse> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(FIND_BY_LECTURER)) {
            ps.setInt(1, lecturerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LecturerCourse c = new LecturerCourse();
                    c.setCourseId(rs.getInt("course_id"));
                    c.setCourseCode(rs.getString("course_code"));
                    c.setCourseName(rs.getString("course_name"));
                    c.setCreditsTheory(rs.getInt("credits_theory"));
                    c.setCreditsPractical(rs.getInt("credits_practical"));
                    c.setSemester(rs.getString("semester"));
                    c.setCourseType(rs.getString("course_type"));
                    c.setStudentCount(rs.getInt("student_count"));
                    list.add(c);
                }
            }
        }
        return list;
    }
}
