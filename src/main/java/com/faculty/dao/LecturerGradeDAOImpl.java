package com.faculty.dao;

import com.faculty.config.DBConnection;
import com.faculty.model.lecture.GradeLine;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LecturerGradeDAOImpl implements LecturerGradeDAO {

    private static final String FIND_ALL =
        "SELECT g.stu_id, ug.reg_number, CONCAT(us.first_name, ' ', us.last_name) AS full_name, ug.status, "
      + "       c.course_code, c.course_name, (c.credits_theory + c.credits_practical) AS credits, "
      + "       g.semester, g.final_mark, g.grade, g.grade_point "
      + "FROM grade g "
      + "JOIN undergraduate ug ON ug.stu_id = g.stu_id "
      + "JOIN users us ON us.user_id = g.stu_id "
      + "JOIN course c ON c.course_id = g.course_id "
      + "ORDER BY ug.reg_number, g.semester, c.course_code";

    @Override
    public List<GradeLine> findAllLines() throws SQLException {
        List<GradeLine> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(FIND_ALL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                GradeLine l = new GradeLine();
                l.setStuId(rs.getInt("stu_id"));
                l.setRegNumber(rs.getString("reg_number"));
                l.setStudentName(rs.getString("full_name"));
                l.setStatus(rs.getString("status"));
                l.setCourseCode(rs.getString("course_code"));
                l.setCourseName(rs.getString("course_name"));
                l.setCredits(rs.getInt("credits"));
                l.setSemester(rs.getString("semester"));
                l.setFinalMark(rs.getBigDecimal("final_mark"));
                l.setGrade(rs.getString("grade"));
                l.setGradePoint(rs.getBigDecimal("grade_point"));
                list.add(l);
            }
        }
        return list;
    }

    @Override
    public String findCourseSemester(int courseId) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT semester FROM course WHERE course_id = ?")) {
            ps.setInt(1, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) : null;
            }
        }
    }

    @Override
    public void saveGrade(int stuId, int courseId, BigDecimal finalMark, String grade, BigDecimal gradePoint,
                          String semester) throws SQLException {
        try (Connection con = DBConnection.getConnection()) {
            int gradeId = -1;
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT grade_id FROM grade WHERE stu_id = ? AND course_id = ? LIMIT 1")) {
                ps.setInt(1, stuId);
                ps.setInt(2, courseId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        gradeId = rs.getInt(1);
                    }
                }
            }
            if (gradeId > 0) {
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE grade SET final_mark = ?, grade = ?, grade_point = ?, semester = ? WHERE grade_id = ?")) {
                    ps.setBigDecimal(1, finalMark);
                    ps.setString(2, grade);
                    ps.setBigDecimal(3, gradePoint);
                    ps.setString(4, semester);
                    ps.setInt(5, gradeId);
                    ps.executeUpdate();
                }
            } else {
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO grade (stu_id, course_id, final_mark, grade, grade_point, semester) VALUES (?,?,?,?,?,?)")) {
                    ps.setInt(1, stuId);
                    ps.setInt(2, courseId);
                    ps.setBigDecimal(3, finalMark);
                    ps.setString(4, grade);
                    ps.setBigDecimal(5, gradePoint);
                    ps.setString(6, semester);
                    ps.executeUpdate();
                }
            }
        }
    }

    @Override
    public void deleteGrade(int stuId, int courseId) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM grade WHERE stu_id = ? AND course_id = ?")) {
            ps.setInt(1, stuId);
            ps.setInt(2, courseId);
            ps.executeUpdate();
        }
    }
}
