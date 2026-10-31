package com.faculty.dao.admin;

import com.faculty.model.admin.AdminMark;

import java.sql.SQLException;
import java.util.List;

/** ABSTRACTION: marks management. */
public interface AdminMarkDAO {
    List<AdminMark> search(String text, int limit) throws SQLException;

    /** @return stu_id for the registration number, or null when there is no such student */
    Integer findStudentIdByReg(String regNumber) throws SQLException;

    void insert(int stuId, int courseId, String evaluationType, String category, double value) throws SQLException;
    void update(int markId, int courseId, String evaluationType, String category, double value) throws SQLException;
    void delete(int markId) throws SQLException;
}
