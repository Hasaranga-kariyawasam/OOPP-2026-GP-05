package com.faculty.dao.admin;

import com.faculty.model.admin.AdminCourse;

import java.sql.SQLException;
import java.util.List;

public class AdminCourseDAOImpl implements AdminCourseDAO {

    private static final String SEARCH =
        "SELECT c.course_id, c.course_code, c.course_name, c.credits_theory, c.credits_practical, c.semester, "
      + "       c.course_type, c.lecture_id, COALESCE(CONCAT(u.first_name,' ',u.last_name),'-') AS lecturer_name "
      + "FROM course c LEFT JOIN users u ON u.user_id = c.lecture_id "
      + "WHERE c.course_code LIKE ? OR c.course_name LIKE ? ORDER BY c.course_code";

    @Override
    public List<AdminCourse> search(String text) throws SQLException {
        String like = Jdbc.like(text);
        return Jdbc.query(SEARCH, rs -> {
            int lecturerId = rs.getInt("lecture_id");
            Integer lecturer = rs.wasNull() ? null : lecturerId;
            return new AdminCourse(rs.getInt("course_id"), rs.getString("course_code"), rs.getString("course_name"),
                    rs.getInt("credits_theory"), rs.getInt("credits_practical"), rs.getString("semester"),
                    rs.getString("course_type"), lecturer, rs.getString("lecturer_name"));
        }, like, like);
    }

    @Override
    public void insert(AdminCourse c, Integer createdByAdminId) throws SQLException {
        Jdbc.update("INSERT INTO course (course_code, course_name, credits_theory, credits_practical, semester, "
                  + "course_type, lecture_id, created_by) VALUES (?,?,?,?,?,?,?,?)",
                c.code(), c.name(), c.theoryCredits(), c.practicalCredits(), c.semester(), c.type(),
                c.lecturerId(), createdByAdminId);
    }

    @Override
    public void update(AdminCourse c) throws SQLException {
        Jdbc.update("UPDATE course SET course_code=?, course_name=?, credits_theory=?, credits_practical=?, "
                  + "semester=?, course_type=?, lecture_id=? WHERE course_id=?",
                c.code(), c.name(), c.theoryCredits(), c.practicalCredits(), c.semester(), c.type(),
                c.lecturerId(), c.courseId());
    }

    @Override
    public void delete(int courseId) throws SQLException {
        Jdbc.update("DELETE FROM course WHERE course_id=?", courseId);
    }
}
