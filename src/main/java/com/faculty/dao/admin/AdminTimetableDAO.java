package com.faculty.dao.admin;

import com.faculty.model.admin.AdminTimetableSlot;

import java.sql.SQLException;
import java.util.List;

/** ABSTRACTION: timetable management. */
public interface AdminTimetableDAO {
    List<AdminTimetableSlot> findAll() throws SQLException;
    void insert(AdminTimetableSlot slot, Integer createdByAdminId) throws SQLException;
    void update(AdminTimetableSlot slot) throws SQLException;
    void delete(int timetableId) throws SQLException;
}
