package com.faculty.service;

import com.faculty.dao.AttendanceDAO;
import com.faculty.dao.AttendanceDAOImpl;
import com.faculty.dao.MedicalDAO;
import com.faculty.dao.MedicalDAOImpl;
import com.faculty.exception.ServiceException;
import com.faculty.model.ApprovalStatus;

import java.sql.SQLException;

/** Numbers shown on the Technical Officer overview page (attendance and medical for now). */
public class TODashboardService {

    /** Pending medicals and the attendance records this officer has entered. */
    public record Stats(int pendingMedicals, int recordsEntered) { }

    private final MedicalDAO medicalDAO = new MedicalDAOImpl();
    private final AttendanceDAO attendanceDAO = new AttendanceDAOImpl();

    public Stats loadStats(int toId) throws ServiceException {
        try {
            int pending = medicalDAO.countByStatus(ApprovalStatus.PENDING);
            int records = attendanceDAO.countRecordedBy(toId);
            return new Stats(pending, records);
        } catch (SQLException e) {
            throw ServiceSupport.wrap("load the dashboard numbers", e);
        }
    }
}
