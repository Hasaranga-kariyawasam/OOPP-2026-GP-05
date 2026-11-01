package com.faculty.dao.admin;

import com.faculty.model.admin.AdminOption;

import java.sql.SQLException;
import java.util.List;

public class AdminLookupDAOImpl implements AdminLookupDAO {

    @Override
    public List<AdminOption> departments() throws SQLException {
        return Jdbc.query("SELECT dep_id, department_name FROM department ORDER BY department_name",
                rs -> new AdminOption(rs.getInt(1), rs.getString(2)));
    }

    @Override
    public List<AdminOption> lecturers() throws SQLException {
        return Jdbc.query("SELECT u.user_id, CONCAT(u.first_name,' ',u.last_name) FROM users u "
                        + "JOIN lecturer l ON l.lecture_id = u.user_id ORDER BY u.first_name",
                rs -> new AdminOption(rs.getInt(1), rs.getString(2)));
    }

    @Override
    public List<AdminOption> courses() throws SQLException {
        return Jdbc.query("SELECT course_id, CONCAT(course_code,' - ',course_name) FROM course ORDER BY course_code",
                rs -> new AdminOption(rs.getInt(1), rs.getString(2)));
    }
}
