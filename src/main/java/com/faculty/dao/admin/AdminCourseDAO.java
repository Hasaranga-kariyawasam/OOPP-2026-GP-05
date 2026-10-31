package com.faculty.dao.admin;

import com.faculty.model.admin.AdminCourse;

import java.sql.SQLException;
import java.util.List;

/** ABSTRACTION: course management. */
public interface AdminCourseDAO {
    List<AdminCourse> search(String text) throws SQLException;
    void insert(AdminCourse course, Integer createdByAdminId) throws SQLException;
    void update(AdminCourse course) throws SQLException;
    void delete(int courseId) throws SQLException;
}
