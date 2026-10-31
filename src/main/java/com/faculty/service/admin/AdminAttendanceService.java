package com.faculty.service.admin;

import com.faculty.dao.admin.AdminAttendanceDAO;
import com.faculty.dao.admin.AdminAttendanceDAOImpl;
import com.faculty.exception.AdminValidationException;
import com.faculty.model.admin.AdminAttendanceRow;

import java.sql.SQLException;
import java.util.List;

/** Business rules for the admin attendance screen: correct a record to Present or Absent. */
public class AdminAttendanceService {

    public static final int SEARCH_LIMIT = 500;

    private final AdminAttendanceDAO attendanceDAO = new AdminAttendanceDAOImpl();

    public List<AdminAttendanceRow> search(String text) throws SQLException {
        AdminSupport.requireAdmin();
        return attendanceDAO.search(AdminSupport.text(text), SEARCH_LIMIT);
    }

    public void markPresent(int attendanceId) throws AdminValidationException, SQLException {
        setStatus(attendanceId, "PRESENT");
    }

    public void markAbsent(int attendanceId) throws AdminValidationException, SQLException {
        setStatus(attendanceId, "ABSENT");
    }

    private void setStatus(int attendanceId, String status) throws AdminValidationException, SQLException {
        AdminSupport.requireAdmin();
        AdminSupport.requireSelected(attendanceId, "an attendance record");
        attendanceDAO.updateStatus(attendanceId, status);
    }
}
