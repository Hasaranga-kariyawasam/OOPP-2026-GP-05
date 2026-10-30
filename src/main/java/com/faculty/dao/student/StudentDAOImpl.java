package com.faculty.dao.student;

import com.faculty.config.DBConnection;
import com.faculty.model.student.StudentRecords.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** JDBC implementation. Every value goes through a '?' placeholder (SEC-04). */
public class StudentDAOImpl implements StudentDAO {

    private static final Map<String, String> DAYS = Map.of(
            "MON", "Monday", "TUE", "Tuesday", "WED", "Wednesday", "THU", "Thursday",
            "FRI", "Friday", "SAT", "Saturday", "SUN", "Sunday");

    /** Turns one ResultSet row into an object. */
    @FunctionalInterface
    private interface RowMapper<T> {
        T map(ResultSet rs) throws SQLException;
    }

    private <T> List<T> select(String sql, RowMapper<T> mapper, Object... params) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<T> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(mapper.map(rs));
                }
                return list;
            }
        }
    }

    private static String nz(String s) {
        return s == null ? "" : s;
    }

    // ------------------------------------------------------------------ courses

    @Override
    public List<CourseInfo> findCourses(int stuId) throws SQLException {
        String sql = "SELECT c.course_id, c.course_code, c.course_name, c.credits_theory, c.credits_practical, "
                   + "       c.semester, c.course_type, CONCAT(u.first_name, ' ', u.last_name) AS lecturer, e.academic_year "
                   + "FROM enrollment e "
                   + "JOIN course c ON c.course_id = e.course_id "
                   + "LEFT JOIN users u ON u.user_id = c.lecture_id "
                   + "WHERE e.stu_id = ? "
                   + "ORDER BY e.academic_year DESC, c.course_code";
        return select(sql, rs -> new CourseInfo(
                rs.getInt("course_id"), rs.getString("course_code"), rs.getString("course_name"),
                rs.getInt("credits_theory"), rs.getInt("credits_practical"), rs.getString("semester"),
                rs.getString("course_type"), nz(rs.getString("lecturer")), rs.getString("academic_year")), stuId);
    }

    @Override
    public List<Material> findMaterials(int courseId) throws SQLException {
        String sql = "SELECT title, upload_date, file_path FROM course_material "
                   + "WHERE course_id = ? ORDER BY upload_date DESC";
        DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.ENGLISH);
        return select(sql, rs -> new Material(rs.getString("title"),
                rs.getTimestamp("upload_date").toLocalDateTime().format(f),
                rs.getString("file_path")), courseId);
    }

    // --------------------------------------------------------------- attendance

    @Override
    public List<AttendanceCount> findAttendanceCounts(int stuId) throws SQLException {
        // An absence covered by an APPROVED medical counts as attended.
        String sql = "SELECT c.course_id, c.course_code, c.course_name, a.session_type, "
                   + "       COUNT(*) AS recorded, "
                   + "       SUM(a.status = 'PRESENT') AS present, "
                   + "       SUM(a.status = 'ABSENT' AND m.approval_status = 'APPROVED') AS med_ok "
                   + "FROM attendance a "
                   + "JOIN course c ON c.course_id = a.course_id "
                   + "LEFT JOIN medical_record m ON m.medical_id = a.medical_id "
                   + "WHERE a.stu_id = ? "
                   + "GROUP BY c.course_id, c.course_code, c.course_name, a.session_type "
                   + "ORDER BY c.course_code, a.session_type";
        return select(sql, rs -> new AttendanceCount(
                rs.getInt("course_id"), rs.getString("course_code"), rs.getString("course_name"),
                rs.getString("session_type"), rs.getInt("recorded"), rs.getInt("present"),
                rs.getInt("med_ok")), stuId);
    }

    @Override
    public List<SessionRow> findSessions(int stuId, int courseId) throws SQLException {
        String sql = "SELECT a.session_id, a.session_type, a.session_date, a.status, m.approval_status "
                   + "FROM attendance a "
                   + "LEFT JOIN medical_record m ON m.medical_id = a.medical_id "
                   + "WHERE a.stu_id = ? AND a.course_id = ? "
                   + "ORDER BY a.session_type, a.session_id";
        return select(sql, rs -> new SessionRow(
                rs.getInt("session_id"), rs.getString("session_type"), rs.getString("session_date"),
                rs.getString("status"),
                rs.getString("approval_status") == null ? "-" : rs.getString("approval_status")),
                stuId, courseId);
    }

    // ------------------------------------------------------------------ medical

    @Override
    public List<MedicalInfo> findMedicals(int stuId) throws SQLException {
        String sql = "SELECT medical_id, session_date, reason, approval_status, document_path "
                   + "FROM medical_record WHERE stu_id = ? ORDER BY session_date DESC";
        return select(sql, rs -> new MedicalInfo(rs.getInt("medical_id"), rs.getString("session_date"),
                nz(rs.getString("reason")), rs.getString("approval_status"),
                nz(rs.getString("document_path"))), stuId);
    }

    @Override
    public int submitMedical(int stuId, LocalDate sessionDate, String reason, String documentPath) throws SQLException {
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement check = con.prepareStatement(
                        "SELECT COUNT(*) FROM medical_record "
                      + "WHERE stu_id = ? AND session_date = ? AND approval_status <> 'REJECTED'")) {
                    check.setInt(1, stuId);
                    check.setDate(2, java.sql.Date.valueOf(sessionDate));
                    try (ResultSet rs = check.executeQuery()) {
                        if (rs.next() && rs.getInt(1) > 0) {
                            con.rollback();
                            return -1;
                        }
                    }
                }

                int medicalId;
                try (PreparedStatement insert = con.prepareStatement(
                        "INSERT INTO medical_record (stu_id, document_path, session_date, reason, approval_status) "
                      + "VALUES (?, ?, ?, ?, 'PENDING')", Statement.RETURN_GENERATED_KEYS)) {
                    insert.setInt(1, stuId);
                    insert.setString(2, documentPath);
                    insert.setDate(3, java.sql.Date.valueOf(sessionDate));
                    insert.setString(4, reason);
                    insert.executeUpdate();
                    try (ResultSet keys = insert.getGeneratedKeys()) {
                        keys.next();
                        medicalId = keys.getInt(1);
                    }
                }

                // cover the absences of that day (they only count once the medical is APPROVED)
                try (PreparedStatement link = con.prepareStatement(
                        "UPDATE attendance SET medical_id = ? "
                      + "WHERE stu_id = ? AND session_date = ? AND status = 'ABSENT' AND medical_id IS NULL")) {
                    link.setInt(1, medicalId);
                    link.setInt(2, stuId);
                    link.setDate(3, java.sql.Date.valueOf(sessionDate));
                    link.executeUpdate();
                }

                con.commit();
                return medicalId;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    // ------------------------------------------------------------------- grades

    @Override
    public List<GradeRow> findGrades(int stuId) throws SQLException {
        // CA % = average of the CA marks of the course (every mark is out of 100).
        String sql = "SELECT c.course_code, c.course_name, (c.credits_theory + c.credits_practical) AS credits, "
                   + "       g.semester, ca.ca_avg, g.final_mark, g.grade, g.grade_point "
                   + "FROM grade g "
                   + "JOIN course c ON c.course_id = g.course_id "
                   + "LEFT JOIN (SELECT course_id, AVG(marks_value) AS ca_avg FROM marks "
                   + "           WHERE stu_id = ? AND mark_category = 'CA' GROUP BY course_id) ca "
                   + "       ON ca.course_id = g.course_id "
                   + "WHERE g.stu_id = ? "
                   + "ORDER BY g.semester, c.course_code";
        return select(sql, rs -> {
            double ca = rs.getDouble("ca_avg");
            if (rs.wasNull()) {
                ca = -1;
            }
            return new GradeRow(rs.getString("course_code"), rs.getString("course_name"),
                    rs.getInt("credits"), rs.getString("semester"), ca,
                    rs.getDouble("final_mark"), rs.getString("grade"), rs.getDouble("grade_point"));
        }, stuId, stuId);
    }

    // ---------------------------------------------------------------- timetable

    @Override
    public List<TimetableRow> findTimetable(int stuId) throws SQLException {
        String sql = "SELECT t.day, t.start_time, t.end_time, c.course_code, c.course_name, "
                   + "       t.session_type, t.venue "
                   + "FROM timetable t "
                   + "JOIN course c ON c.course_id = t.course_id "
                   + "WHERE t.course_id IN (SELECT e.course_id FROM enrollment e WHERE e.stu_id = ?) "
                   + "ORDER BY FIELD(t.day,'MON','TUE','WED','THU','FRI','SAT','SUN'), t.start_time";
        return select(sql, rs -> new TimetableRow(
                DAYS.getOrDefault(rs.getString("day"), rs.getString("day")),
                rs.getString("start_time").substring(0, 5), rs.getString("end_time").substring(0, 5),
                rs.getString("course_code"), rs.getString("course_name"),
                rs.getString("session_type"), nz(rs.getString("venue"))), stuId);
    }

    // ------------------------------------------------------------------ notices

    @Override
    public List<NoticeInfo> findNotices(int depId) throws SQLException {
        String sql = "SELECT title, content, posted_date FROM notice "
                   + "WHERE target_role IN ('ALL', 'UNDERGRADUATE') AND (dep_id IS NULL OR dep_id = ?) "
                   + "ORDER BY posted_date DESC";
        DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.ENGLISH);
        return select(sql, rs -> new NoticeInfo(rs.getString("title"), rs.getString("content"),
                rs.getTimestamp("posted_date").toLocalDateTime().format(f)), depId);
    }

    // ------------------------------------------------------------------ profile

    @Override
    public boolean updateContact(int userId, String email, String phone, String street,
                                 String city, String postalCode, String country) throws SQLException {
        String sql = "UPDATE users SET email = ?, phone = ?, street = ?, city = ?, postal_code = ?, country = ? "
                   + "WHERE user_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, phone);
            ps.setString(3, street);
            ps.setString(4, city);
            ps.setString(5, postalCode);
            ps.setString(6, country);
            ps.setInt(7, userId);
            ps.executeUpdate();
            return true;
        } catch (SQLIntegrityConstraintViolationException e) {
            return false;   // duplicate e-mail
        }
    }

    @Override
    public void updatePicture(int userId, String picturePath) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "UPDATE users SET profile_picture_path = ? WHERE user_id = ?")) {
            ps.setString(1, picturePath);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }
}
