package com.faculty.dao.admin;

import com.faculty.model.admin.AdminMark;

import java.sql.SQLException;
import java.util.List;

public class AdminMarkDAOImpl implements AdminMarkDAO {

    private static final String SEARCH =
        "SELECT m.mark_id, m.course_id, ug.reg_number, c.course_code, m.evaluation_type, m.mark_category, m.marks_value "
      + "FROM marks m JOIN undergraduate ug ON ug.stu_id = m.stu_id "
      + "JOIN course c ON c.course_id = m.course_id "
      + "WHERE ug.reg_number LIKE ? OR c.course_code LIKE ? "
      + "ORDER BY ug.reg_number, c.course_code, m.mark_id LIMIT ?";

    @Override
    public List<AdminMark> search(String text, int limit) throws SQLException {
        String like = Jdbc.like(text);
        return Jdbc.query(SEARCH, rs -> new AdminMark(rs.getInt("mark_id"), rs.getInt("course_id"),
                rs.getString("reg_number"), rs.getString("course_code"), rs.getString("evaluation_type"),
                rs.getString("mark_category"), rs.getDouble("marks_value")), like, like, limit);
    }

    @Override
    public Integer findStudentIdByReg(String regNumber) throws SQLException {
        List<Integer> found = Jdbc.query("SELECT stu_id FROM undergraduate WHERE reg_number=?",
                rs -> rs.getInt(1), regNumber);
        return found.isEmpty() ? null : found.get(0);
    }

    @Override
    public void insert(int stuId, int courseId, String evaluationType, String category, double value) throws SQLException {
        Jdbc.update("INSERT INTO marks (stu_id, course_id, evaluation_type, mark_category, marks_value) VALUES (?,?,?,?,?)",
                stuId, courseId, evaluationType, category, value);
    }

    @Override
    public void update(int markId, int courseId, String evaluationType, String category, double value) throws SQLException {
        Jdbc.update("UPDATE marks SET course_id=?, evaluation_type=?, mark_category=?, marks_value=? WHERE mark_id=?",
                courseId, evaluationType, category, value, markId);
    }

    @Override
    public void delete(int markId) throws SQLException {
        Jdbc.update("DELETE FROM marks WHERE mark_id=?", markId);
    }
}
