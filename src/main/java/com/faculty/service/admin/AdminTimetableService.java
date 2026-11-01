package com.faculty.service.admin;

import com.faculty.dao.admin.AdminTimetableDAO;
import com.faculty.dao.admin.AdminTimetableDAOImpl;
import com.faculty.exception.AdminValidationException;
import com.faculty.model.admin.AdminTimetableForm;
import com.faculty.model.admin.AdminTimetableSlot;

import java.sql.SQLException;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;

/** Business rules for timetable management. */
public class AdminTimetableService {

    public static final List<String> DAYS = List.of("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN");
    public static final List<String> SESSION_TYPES = List.of("THEORY", "PRACTICAL");

    private final AdminTimetableDAO timetableDAO = new AdminTimetableDAOImpl();

    public List<AdminTimetableSlot> findAll() throws SQLException {
        AdminSupport.requireAdmin();
        return timetableDAO.findAll();
    }

    public void add(AdminTimetableForm form) throws AdminValidationException, SQLException {
        int adminId = AdminSupport.currentAdminId();
        timetableDAO.insert(validate(0, form), adminId);
    }

    public void update(int timetableId, AdminTimetableForm form) throws AdminValidationException, SQLException {
        AdminSupport.requireAdmin();
        AdminSupport.requireSelected(timetableId, "a session");
        timetableDAO.update(validate(timetableId, form));
    }

    public void delete(int timetableId) throws AdminValidationException, SQLException {
        AdminSupport.requireAdmin();
        AdminSupport.requireSelected(timetableId, "a session");
        timetableDAO.delete(timetableId);
    }

    private AdminTimetableSlot validate(int id, AdminTimetableForm f) throws AdminValidationException {
        AdminSupport.require(f.courseId() > 0, "Choose a course.");
        AdminSupport.require(f.depId() > 0, "Choose a department.");
        AdminSupport.require(f.day() != null && DAYS.contains(f.day()), "Choose a day.");
        AdminSupport.require(f.sessionType() != null && SESSION_TYPES.contains(f.sessionType()), "Choose a session type.");

        LocalTime start;
        LocalTime end;
        try {
            start = LocalTime.parse(AdminSupport.text(f.startText()));
            end = LocalTime.parse(AdminSupport.text(f.endText()));
        } catch (DateTimeParseException e) {
            throw new AdminValidationException("Enter times like 08:30 (24-hour, HH:mm).");
        }
        AdminSupport.require(end.isAfter(start), "End time must be after start time.");

        String venue = AdminSupport.text(f.venue());
        AdminSupport.require(venue.length() <= 50, "Venue is too long (50 characters max).");
        return new AdminTimetableSlot(id, f.courseId(), f.depId(), null, null, f.day(), start, end,
                f.sessionType(), venue);
    }
}
