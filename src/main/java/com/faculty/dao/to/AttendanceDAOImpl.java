package com.faculty.dao.to;

import com.faculty.config.DBConnection;
import com.faculty.model.to.ApprovalStatus;
import com.faculty.model.to.Attendance;
import com.faculty.model.to.AttendanceSheetRow;
import com.faculty.model.to.AttendanceStatus;
import com.faculty.model.to.AttendanceSummary;
import com.faculty.model.to.SessionType;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** JDBC implementation of AttendanceDAO. Every query uses '?' placeholders (SEC-04). */
public class AttendanceDAOImpl implements AttendanceDAO {

    /** 1 session = 2 hours (project brief). */
    private static final double SESSION_HOURS = 2.0;

    private static final String FIND_SHEET =
        "SELECT ug.stu_id, ug.reg_number, u.first_name, u.last_name, a.status AS att_status, "
      + "       (SELECT m.approval_status FROM medical_record m "
      + "         WHERE m.stu_id = ug.stu_id AND m.session_date = ? "
      + "         ORDER BY (m.approval_status = 'APPROVED') DESC, m.medical_id DESC LIMIT 1) AS med_status "
      + "FROM undergraduate ug "
      + "JOIN users u ON u.user_id = ug.stu_id "
      + "LEFT JOIN attendance a ON a.stu_id = ug.stu_id AND a.course_id = ? "
      + "                      AND a.session_type = ? AND a.session_date = ? "
      + "WHERE u.is_active = TRUE "
      + "  AND ug.stu_id IN (SELECT e.stu_id FROM enrollment e WHERE e.course_id = ?) "
      + "ORDER BY ug.reg_number";

    // Insert, or update when (student, course, type, date) already exists (uq_attendance).
    // An ABSENT row is linked to the student's medical of that date, if there is one.
    private static final String UPSERT =
        "INSERT INTO attendance (stu_id, course_id, to_id, medical_id, session_id, session_type, "
      + "                        session_date, status, session_hours) "
      + "VALUES (?, ?, ?, "
      + "  CASE WHEN ? = 'ABSENT' THEN "
      + "    (SELECT m.medical_id FROM medical_record m WHERE m.stu_id = ? AND m.session_date = ? "
      + "      ORDER BY (m.approval_status = 'APPROVED') DESC, m.medical_id DESC LIMIT 1) "
      + "  ELSE NULL END, "
      + "  ?, ?, ?, ?, ?) "
      + "ON DUPLICATE KEY UPDATE to_id = VALUES(to_id), medical_id = VALUES(medical_id), "
      + "                        session_id = VALUES(session_id), status = VALUES(status)";

    @Override
    public List<AttendanceSheetRow> findSheet(int courseId, SessionType type, LocalDate date) throws SQLException {
        List<AttendanceSheetRow> rows = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(FIND_SHEET)) {

            ps.setDate(1, Date.valueOf(date));
            ps.setInt(2, courseId);
            ps.setString(3, type.name());
            ps.setDate(4, Date.valueOf(date));
            ps.setInt(5, courseId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AttendanceSheetRow row = new AttendanceSheetRow(
                            rs.getInt("stu_id"),
                            rs.getString("reg_number"),
                            rs.getString("first_name") + " " + rs.getString("last_name"));

                    String saved = rs.getString("att_status");
                    if (saved != null) {
                        row.setStatus(AttendanceStatus.valueOf(saved));
                        row.setAlreadySaved(true);
                    }
                    String med = rs.getString("med_status");
                    if (med != null) {
                        row.setMedicalStatus(ApprovalStatus.valueOf(med));
                    }
                    rows.add(row);
                }
            }
        }
        return rows;
    }

    @Override
    public Map<Integer, LocalDate> findRecordedSessions(int courseId, SessionType type) throws SQLException {
        String sql = "SELECT session_id, MIN(session_date) AS session_date FROM attendance "
                   + "WHERE course_id = ? AND session_type = ? GROUP BY session_id ORDER BY session_id";
        Map<Integer, LocalDate> sessions = new LinkedHashMap<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, courseId);
            ps.setString(2, type.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    sessions.put(rs.getInt("session_id"), rs.getDate("session_date").toLocalDate());
                }
            }
        }
        return sessions;
    }

    @Override
    public void saveSheet(int toId, int courseId, SessionType type, int sessionNo, LocalDate date,
                          List<AttendanceSheetRow> rows) throws SQLException {
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);                       // all students or none
            try (PreparedStatement ps = con.prepareStatement(UPSERT)) {
                for (AttendanceSheetRow row : rows) {
                    String status = row.getStatus().name();
                    ps.setInt(1, row.getStuId());
                    ps.setInt(2, courseId);
                    ps.setInt(3, toId);
                    ps.setString(4, status);                // CASE WHEN ? = 'ABSENT'
                    ps.setInt(5, row.getStuId());           // medical lookup: student
                    ps.setDate(6, Date.valueOf(date));      // medical lookup: date
                    ps.setInt(7, sessionNo);
                    ps.setString(8, type.name());
                    ps.setDate(9, Date.valueOf(date));
                    ps.setString(10, status);
                    ps.setDouble(11, SESSION_HOURS);
                    ps.addBatch();
                }
                ps.executeBatch();
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    @Override
    public int deleteSession(int courseId, SessionType type, LocalDate date) throws SQLException {
        String sql = "DELETE FROM attendance WHERE course_id = ? AND session_type = ? AND session_date = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, courseId);
            ps.setString(2, type.name());
            ps.setDate(3, Date.valueOf(date));
            return ps.executeUpdate();
        }
    }

    @Override
    public List<AttendanceSummary> findSummaries(Integer courseId, Integer stuId, SessionType type, String batch)
            throws SQLException {

        StringBuilder sql = new StringBuilder(
            "SELECT ug.stu_id, ug.reg_number, u.first_name, u.last_name, ug.batch, ug.status AS ug_status, "
          + "       c.course_id, c.course_code, c.course_name, "
          + "       COALESCE(SUM(CASE WHEN a.status = 'PRESENT' THEN a.session_hours END), 0) AS present_hours, "
          + "       COALESCE(SUM(a.session_hours), 0) AS total_hours, "
          + "       COALESCE(SUM(CASE WHEN a.status = 'ABSENT' AND m.approval_status = 'APPROVED' "
          + "                         THEN a.session_hours END), 0) AS excused_hours, "
          + "       COUNT(a.attendance_id) AS total_sessions, "
          + "       COALESCE(SUM(a.status = 'PRESENT'), 0) AS present_sessions, "
          + "       COALESCE(SUM(a.status = 'ABSENT' AND m.medical_id IS NOT NULL), 0) AS medical_sessions "
          + "FROM (SELECT DISTINCT stu_id, course_id FROM enrollment) e "
          + "JOIN undergraduate ug ON ug.stu_id = e.stu_id "
          + "JOIN users u ON u.user_id = ug.stu_id "
          + "JOIN course c ON c.course_id = e.course_id "
          + "LEFT JOIN attendance a ON a.stu_id = e.stu_id AND a.course_id = e.course_id ");

        List<Object> params = new ArrayList<>();
        if (type != null) {
            sql.append("AND a.session_type = ? ");
            params.add(type.name());
        }
        sql.append("LEFT JOIN medical_record m ON m.medical_id = a.medical_id WHERE u.is_active = TRUE ");
        if (courseId != null) {
            sql.append("AND e.course_id = ? ");
            params.add(courseId);
        }
        if (stuId != null) {
            sql.append("AND e.stu_id = ? ");
            params.add(stuId);
        }
        if (batch != null) {
            sql.append("AND ug.batch = ? ");
            params.add(batch);
        }
        sql.append("GROUP BY ug.stu_id, ug.reg_number, u.first_name, u.last_name, ug.batch, ug.status, "
                 + "         c.course_id, c.course_code, c.course_name "
                 + "ORDER BY c.course_code, ug.reg_number");

        List<AttendanceSummary> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AttendanceSummary s = new AttendanceSummary();
                    s.setStuId(rs.getInt("stu_id"));
                    s.setRegNumber(rs.getString("reg_number"));
                    s.setStudentName(rs.getString("first_name") + " " + rs.getString("last_name"));
                    s.setBatch(rs.getString("batch"));
                    s.setStudentStatus(rs.getString("ug_status"));
                    s.setCourseId(rs.getInt("course_id"));
                    s.setCourseCode(rs.getString("course_code"));
                    s.setCourseName(rs.getString("course_name"));
                    s.setPresentHours(rs.getDouble("present_hours"));
                    s.setTotalHours(rs.getDouble("total_hours"));
                    s.setExcusedHours(rs.getDouble("excused_hours"));
                    s.setTotalSessions(rs.getInt("total_sessions"));
                    s.setPresentSessions(rs.getInt("present_sessions"));
                    s.setMedicalSessions(rs.getInt("medical_sessions"));
                    list.add(s);
                }
            }
        }
        return list;
    }

    @Override
    public List<Attendance> findByStudentAndCourse(int stuId, int courseId) throws SQLException {
        String sql = "SELECT a.attendance_id, a.stu_id, a.course_id, a.to_id, a.medical_id, a.session_id, "
                   + "       a.session_type, a.session_date, a.status, a.session_hours, m.approval_status AS med_status "
                   + "FROM attendance a LEFT JOIN medical_record m ON m.medical_id = a.medical_id "
                   + "WHERE a.stu_id = ? AND a.course_id = ? "
                   + "ORDER BY a.session_type, a.session_id";
        List<Attendance> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, stuId);
            ps.setInt(2, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Attendance a = new Attendance();
                    a.setAttendanceId(rs.getInt("attendance_id"));
                    a.setStuId(rs.getInt("stu_id"));
                    a.setCourseId(rs.getInt("course_id"));
                    int toId = rs.getInt("to_id");
                    a.setToId(rs.wasNull() ? null : toId);
                    int medId = rs.getInt("medical_id");
                    a.setMedicalId(rs.wasNull() ? null : medId);
                    a.setSessionNo(rs.getInt("session_id"));
                    a.setSessionType(SessionType.valueOf(rs.getString("session_type")));
                    a.setSessionDate(rs.getDate("session_date").toLocalDate());
                    a.setStatus(AttendanceStatus.valueOf(rs.getString("status")));
                    a.setSessionHours(rs.getDouble("session_hours"));
                    String med = rs.getString("med_status");
                    if (med != null) {
                        a.setMedicalStatus(ApprovalStatus.valueOf(med));
                    }
                    list.add(a);
                }
            }
        }
        return list;
    }

    @Override
    public int countRecordedBy(int toId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM attendance WHERE to_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, toId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }
}
