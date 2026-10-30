package com.faculty.dao;

import com.faculty.config.DBConnection;
import com.faculty.model.lecture.CourseStudent;
import com.faculty.model.lecture.MarkEntry;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LecturerMarksDAOImpl implements LecturerMarksDAO {

    // SEC-04: every value goes in through a '?' placeholder.
    private static final String FIND_STUDENTS =
        "SELECT DISTINCT u.stu_id, u.reg_number, CONCAT(us.first_name, ' ', us.last_name) AS full_name "
      + "FROM enrollment e "
      + "JOIN undergraduate u ON u.stu_id = e.stu_id "
      + "JOIN users us ON us.user_id = u.stu_id "
      + "WHERE e.course_id = ? ORDER BY u.reg_number";

    private static final String FIND_BY_COURSE =
        "SELECT m.mark_id, m.stu_id, m.course_id, u.reg_number, "
      + "       CONCAT(us.first_name, ' ', us.last_name) AS full_name, "
      + "       m.evaluation_type, m.mark_category, m.marks_value "
      + "FROM marks m "
      + "JOIN undergraduate u ON u.stu_id = m.stu_id "
      + "JOIN users us ON us.user_id = u.stu_id "
      + "WHERE m.course_id = ? ORDER BY u.reg_number, m.evaluation_type";

    private static final String IS_ENROLLED =
        "SELECT 1 FROM enrollment WHERE stu_id = ? AND course_id = ? LIMIT 1";

    private static final String FIND_MARK_ID =
        "SELECT mark_id FROM marks WHERE stu_id = ? AND course_id = ? AND evaluation_type = ? LIMIT 1";

    private static final String INSERT =
        "INSERT INTO marks (stu_id, course_id, evaluation_type, mark_category, marks_value) VALUES (?,?,?,?,?)";

    // The join to course makes sure a lecturer can only touch marks of his own courses.
    private static final String UPDATE =
        "UPDATE marks m JOIN course c ON c.course_id = m.course_id "
      + "SET m.mark_category = ?, m.marks_value = ? "
      + "WHERE m.mark_id = ? AND c.lecture_id = ?";

    private static final String DELETE =
        "DELETE m FROM marks m JOIN course c ON c.course_id = m.course_id "
      + "WHERE m.mark_id = ? AND c.lecture_id = ?";

    @Override
    public List<CourseStudent> findEnrolledStudents(int courseId) throws SQLException {
        List<CourseStudent> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(FIND_STUDENTS)) {
            ps.setInt(1, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new CourseStudent(rs.getInt("stu_id"), rs.getString("reg_number"), rs.getString("full_name")));
                }
            }
        }
        return list;
    }

    @Override
    public List<MarkEntry> findByCourse(int courseId) throws SQLException {
        List<MarkEntry> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(FIND_BY_COURSE)) {
            ps.setInt(1, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    MarkEntry m = new MarkEntry();
                    m.setMarkId(rs.getInt("mark_id"));
                    m.setStuId(rs.getInt("stu_id"));
                    m.setCourseId(rs.getInt("course_id"));
                    m.setRegNumber(rs.getString("reg_number"));
                    m.setStudentName(rs.getString("full_name"));
                    m.setEvaluationType(rs.getString("evaluation_type"));
                    m.setCategory(rs.getString("mark_category"));
                    m.setMarksValue(rs.getBigDecimal("marks_value"));
                    list.add(m);
                }
            }
        }
        return list;
    }

    @Override
    public boolean isEnrolled(int stuId, int courseId) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(IS_ENROLLED)) {
            ps.setInt(1, stuId);
            ps.setInt(2, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    @Override
    public int findMarkId(int stuId, int courseId, String evaluationType) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(FIND_MARK_ID)) {
            ps.setInt(1, stuId);
            ps.setInt(2, courseId);
            ps.setString(3, evaluationType);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : -1;
            }
        }
    }

    @Override
    public void insert(int stuId, int courseId, String evaluationType, String category, BigDecimal value) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(INSERT)) {
            ps.setInt(1, stuId);
            ps.setInt(2, courseId);
            ps.setString(3, evaluationType);
            ps.setString(4, category);
            ps.setBigDecimal(5, value);
            ps.executeUpdate();
        }
    }

    @Override
    public int update(int markId, int lecturerId, String category, BigDecimal value) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(UPDATE)) {
            ps.setString(1, category);
            ps.setBigDecimal(2, value);
            ps.setInt(3, markId);
            ps.setInt(4, lecturerId);
            return ps.executeUpdate();
        }
    }

    @Override
    public int delete(int markId, int lecturerId) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(DELETE)) {
            ps.setInt(1, markId);
            ps.setInt(2, lecturerId);
            return ps.executeUpdate();
        }
    }
}
