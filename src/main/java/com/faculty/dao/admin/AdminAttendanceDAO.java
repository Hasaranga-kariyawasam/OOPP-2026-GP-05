package com.faculty.dao.admin;

import com.faculty.model.admin.AdminAttendanceRow;

import java.sql.SQLException;
import java.util.List;

/** ABSTRACTION: attendance records seen by the admin. */
public interface AdminAttendanceDAO {
    List<AdminAttendanceRow> search(String text, int limit) throws SQLException;
    void updateStatus(int attendanceId, String status) throws SQLException;
}
