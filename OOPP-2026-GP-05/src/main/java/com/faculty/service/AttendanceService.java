package com.faculty.service;

import com.faculty.dao.AttendanceDAO;
import com.faculty.dao.AttendanceDAOImpl;
import com.faculty.dao.CourseQueryDAO;
import com.faculty.dao.CourseQueryDAOImpl;
import com.faculty.dao.StudentQueryDAO;
import com.faculty.dao.StudentQueryDAOImpl;
import com.faculty.exception.ServiceException;
import com.faculty.exception.ValidationException;
import com.faculty.model.Attendance;
import com.faculty.model.AttendanceSheetRow;
import com.faculty.model.AttendanceSummary;
import com.faculty.model.Course;
import com.faculty.model.SessionType;
import com.faculty.model.StudentCourseAttendance;
import com.faculty.model.student.Undergraduate;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Business rules for attendance (FR-28 .. FR-35): recording a session, correcting it,
 * and the batch / individual summaries (theory, practical, combined).
 */
public class AttendanceService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    private final AttendanceDAO attendanceDAO = new AttendanceDAOImpl();
    private final CourseQueryDAO courseDAO = new CourseQueryDAOImpl();
    private final StudentQueryDAO studentDAO = new StudentQueryDAOImpl();

    // ------------------------------------------------------------------ pick lists

    public List<Course> getCourses() throws ServiceException {
        try {
            return courseDAO.findAll();
        } catch (SQLException e) {
            throw ServiceSupport.wrap("load the courses", e);
        }
    }

    public List<Undergraduate> getStudents() throws ServiceException {
        try {
            return studentDAO.findAllActive();
        } catch (SQLException e) {
            throw ServiceSupport.wrap("load the undergraduates", e);
        }
    }

    public List<String> getBatches() throws ServiceException {
        try {
            return studentDAO.findBatches();
        } catch (SQLException e) {
            throw ServiceSupport.wrap("load the batches", e);
        }
    }

    // ------------------------------------------------------------------ recording (FR-28, FR-29)

    /** session number -> date of the sessions already recorded for this course component. */
    public Map<Integer, LocalDate> getRecordedSessions(Course course, SessionType type) throws ServiceException {
        try {
            return attendanceDAO.findRecordedSessions(course.getCourseId(), type);
        } catch (SQLException e) {
            throw ServiceSupport.wrap("load the recorded sessions", e);
        }
    }

    /** First session number (1..15) that has not been recorded yet; 15 when all are recorded. */
    public int suggestNextSessionNo(Course course, SessionType type) throws ServiceException {
        Map<Integer, LocalDate> recorded = getRecordedSessions(course, type);
        for (int no = 1; no <= AttendanceCalculator.SESSIONS_PER_COMPONENT; no++) {
            if (!recorded.containsKey(no)) {
                return no;
            }
        }
        return AttendanceCalculator.SESSIONS_PER_COMPONENT;
    }

    /** Loads the sheet for a date: saved statuses are shown, everybody else defaults to PRESENT. */
    public List<AttendanceSheetRow> loadSheet(Course course, SessionType type, LocalDate date)
            throws ServiceException {
        try {
            return attendanceDAO.findSheet(course.getCourseId(), type, date);
        } catch (SQLException e) {
            throw ServiceSupport.wrap("load the attendance sheet", e);
        }
    }

    /** Validates and saves one session for every student on the sheet. */
    public void saveSheet(int toId, Course course, SessionType type, int sessionNo, LocalDate date,
                          List<AttendanceSheetRow> rows) throws ValidationException, ServiceException {
        validateSession(course, type, sessionNo, date);
        if (rows == null || rows.isEmpty()) {
            throw new ValidationException("No undergraduates are enrolled in this course.");
        }
        try {
            // the same session number must not be used for two different dates (FR-28)
            LocalDate usedOn = attendanceDAO.findRecordedSessions(course.getCourseId(), type).get(sessionNo);
            if (usedOn != null && !usedOn.equals(date)) {
                throw new ValidationException("Session " + sessionNo + " of this " + type.getLabel().toLowerCase()
                        + " component was already recorded on " + usedOn.format(DATE_FMT)
                        + ". Choose a different session number or date.");
            }
            attendanceDAO.saveSheet(toId, course.getCourseId(), type, sessionNo, date, rows);
        } catch (SQLException e) {
            throw ServiceSupport.wrap("save the attendance", e);
        }
    }

    /** Deletes the whole session of that date; returns the number of records removed. */
    public int deleteSession(Course course, SessionType type, LocalDate date)
            throws ValidationException, ServiceException {
        if (course == null || type == null || date == null) {
            throw new ValidationException("Select the course, session type and date first.");
        }
        try {
            return attendanceDAO.deleteSession(course.getCourseId(), type, date);
        } catch (SQLException e) {
            throw ServiceSupport.wrap("delete the session", e);
        }
    }

    private void validateSession(Course course, SessionType type, int sessionNo, LocalDate date)
            throws ValidationException {
        if (course == null) {
            throw new ValidationException("Please select a course.");
        }
        if (type == null) {
            throw new ValidationException("Please select the session type (theory / practical).");
        }
        if (!course.supports(type)) {
            throw new ValidationException(course.getCourseCode() + " has no " + type.getLabel().toLowerCase()
                    + " component.");
        }
        if (sessionNo < 1 || sessionNo > AttendanceCalculator.SESSIONS_PER_COMPONENT) {
            throw new ValidationException("Session number must be between 1 and "
                    + AttendanceCalculator.SESSIONS_PER_COMPONENT + ".");
        }
        if (date == null) {
            throw new ValidationException("Please select the session date.");
        }
        if (date.isAfter(LocalDate.now())) {
            throw new ValidationException("Attendance cannot be recorded for a future date.");
        }
    }

    // ------------------------------------------------------------------ summaries (FR-30, FR-31, FR-32)

    /**
     * Whole-batch summary of one course.
     * @param type  THEORY, PRACTICAL, or null for theory + practical combined
     * @param batch batch code or null for every batch
     */
    public List<AttendanceSummary> getBatchSummary(Course course, SessionType type, String batch)
            throws ValidationException, ServiceException {
        if (course == null) {
            throw new ValidationException("Please select a course.");
        }
        try {
            return attendanceDAO.findSummaries(course.getCourseId(), null, type, batch);
        } catch (SQLException e) {
            throw ServiceSupport.wrap("load the attendance summary", e);
        }
    }

    /** One undergraduate, every enrolled course, with theory / practical / combined figures. */
    public List<StudentCourseAttendance> getStudentSummary(Undergraduate student)
            throws ValidationException, ServiceException {
        if (student == null) {
            throw new ValidationException("Please select an undergraduate.");
        }
        try {
            int id = student.getUserId();
            Map<Integer, AttendanceSummary> theory = byCourse(attendanceDAO.findSummaries(null, id, SessionType.THEORY, null));
            Map<Integer, AttendanceSummary> practical = byCourse(attendanceDAO.findSummaries(null, id, SessionType.PRACTICAL, null));
            Map<Integer, AttendanceSummary> combined = byCourse(attendanceDAO.findSummaries(null, id, null, null));

            Map<Integer, Course> courses = new LinkedHashMap<>();
            for (Course c : courseDAO.findAll()) {
                courses.put(c.getCourseId(), c);
            }

            List<StudentCourseAttendance> result = new ArrayList<>();
            for (Map.Entry<Integer, AttendanceSummary> e : combined.entrySet()) {
                Course course = courses.get(e.getKey());
                if (course != null) {
                    result.add(new StudentCourseAttendance(course, theory.get(e.getKey()),
                            practical.get(e.getKey()), e.getValue()));
                }
            }
            return result;
        } catch (SQLException e) {
            throw ServiceSupport.wrap("load the student's attendance", e);
        }
    }

    /** Session-by-session log of one student in one course. */
    public List<Attendance> getSessionLog(Undergraduate student, Course course) throws ServiceException {
        try {
            return attendanceDAO.findByStudentAndCourse(student.getUserId(), course.getCourseId());
        } catch (SQLException e) {
            throw ServiceSupport.wrap("load the session log", e);
        }
    }

    public int countRecordedBy(int toId) throws ServiceException {
        try {
            return attendanceDAO.countRecordedBy(toId);
        } catch (SQLException e) {
            throw ServiceSupport.wrap("load the attendance count", e);
        }
    }

    private static Map<Integer, AttendanceSummary> byCourse(List<AttendanceSummary> list) {
        Map<Integer, AttendanceSummary> map = new LinkedHashMap<>();
        for (AttendanceSummary s : list) {
            map.put(s.getCourseId(), s);
        }
        return map;
    }
}
