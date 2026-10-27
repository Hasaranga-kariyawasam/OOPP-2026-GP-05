package com.faculty.service;

import com.faculty.dao.AttendanceDAO;
import com.faculty.dao.AttendanceDAOImpl;
import com.faculty.dao.MedicalDAO;
import com.faculty.dao.MedicalDAOImpl;
import com.faculty.dao.NoticeQueryDAO;
import com.faculty.dao.NoticeQueryDAOImpl;
import com.faculty.dao.TimetableQueryDAO;
import com.faculty.dao.TimetableQueryDAOImpl;
import com.faculty.exception.ServiceException;
import com.faculty.model.ApprovalStatus;
import com.faculty.model.TimetableEntry;

import java.sql.SQLException;
import java.time.DayOfWeek;
import java.util.List;

/** Numbers shown on the Technical Officer overview page. */
public class TODashboardService {

    /** The four numbers of the overview tiles. */
    public record Stats(int pendingMedicals, int sessionsToday, int recordsEntered, int notices) { }

    private final MedicalDAO medicalDAO = new MedicalDAOImpl();
    private final AttendanceDAO attendanceDAO = new AttendanceDAOImpl();
    private final NoticeQueryDAO noticeDAO = new NoticeQueryDAOImpl();
    private final TimetableQueryDAO timetableDAO = new TimetableQueryDAOImpl();

    public Stats loadStats(int toId, int depId, DayOfWeek today) throws ServiceException {
        try {
            int pending = medicalDAO.countByStatus(ApprovalStatus.PENDING);
            int records = attendanceDAO.countRecordedBy(toId);
            int notices = noticeDAO.findForTechnicalOfficer(depId).size();

            String code = TimetableService.dayCode(today);
            List<TimetableEntry> timetable = timetableDAO.findByDepartment(depId);
            int sessionsToday = (int) timetable.stream().filter(t -> t.getDay().equals(code)).count();

            return new Stats(pending, sessionsToday, records, notices);
        } catch (SQLException e) {
            throw ServiceSupport.wrap("load the dashboard numbers", e);
        }
    }
}
