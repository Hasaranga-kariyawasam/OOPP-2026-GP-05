package com.faculty.dao;

import com.faculty.config.DBConnection;
import com.faculty.model.Course;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CourseQueryDAOImpl implements CourseQueryDAO {

    @Override
    public List<Course> findAll() throws SQLException {
        String sql = "SELECT course_id, course_code, course_name, credits_theory, credits_practical, "
                   + "       semester, course_type FROM course ORDER BY course_code";
        List<Course> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Course c = new Course();
                c.setCourseId(rs.getInt("course_id"));
                c.setCourseCode(rs.getString("course_code"));
                c.setCourseName(rs.getString("course_name"));
                c.setCreditsTheory(rs.getInt("credits_theory"));
                c.setCreditsPractical(rs.getInt("credits_practical"));
                c.setSemester(rs.getString("semester"));
                c.setCourseType(rs.getString("course_type"));
                list.add(c);
            }
        }
        return list;
    }
}
