package com.faculty.dao.to;

import com.faculty.config.DBConnection;
import com.faculty.model.to.ApprovalStatus;
import com.faculty.model.to.MedicalRecord;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** JDBC implementation of MedicalDAO. */
public class MedicalDAOImpl implements MedicalDAO {

    private static final String SELECT_BASE =
        "SELECT m.medical_id, m.stu_id, m.document_path, m.session_date, m.reason, m.approval_status, "
      + "       m.approved_by, ug.reg_number, u.first_name, u.last_name, "
      + "       ap.first_name AS ap_first, ap.last_name AS ap_last "
      + "FROM medical_record m "
      + "JOIN undergraduate ug ON ug.stu_id = m.stu_id "
      + "JOIN users u ON u.user_id = m.stu_id "
      + "LEFT JOIN users ap ON ap.user_id = m.approved_by ";

    @Override
    public List<MedicalRecord> findAll(ApprovalStatus status, String search) throws SQLException {
        StringBuilder sql = new StringBuilder(SELECT_BASE).append("WHERE 1 = 1 ");
        List<Object> params = new ArrayList<>();

        if (status != null) {
            sql.append("AND m.approval_status = ? ");
            params.add(status.name());
        }
        if (search != null && !search.isBlank()) {
            sql.append("AND (ug.reg_number LIKE ? OR CONCAT(u.first_name, ' ', u.last_name) LIKE ? "
                     + "     OR m.reason LIKE ?) ");
            String like = "%" + search.trim() + "%";
            params.add(like);
            params.add(like);
            params.add(like);
        }
        sql.append("ORDER BY m.session_date DESC, m.medical_id DESC");

        List<MedicalRecord> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }
        return list;
    }

    @Override
    public MedicalRecord findById(int medicalId) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_BASE + "WHERE m.medical_id = ?")) {
            ps.setInt(1, medicalId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    @Override
    public int insert(MedicalRecord m) throws SQLException {
        String sql = "INSERT INTO medical_record (stu_id, document_path, session_date, reason, approval_status) "
                   + "VALUES (?, ?, ?, ?, 'PENDING')";
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, m.getStuId());
                setNullableString(ps, 2, m.getDocumentPath());
                ps.setDate(3, Date.valueOf(m.getSessionDate()));
                setNullableString(ps, 4, m.getReason());
                ps.executeUpdate();

                int id;
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    keys.next();
                    id = keys.getInt(1);
                }
                relink(con, id, m.getStuId(), m.getSessionDate());
                con.commit();
                return id;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    @Override
    public void update(MedicalRecord m) throws SQLException {
        String sql = "UPDATE medical_record SET document_path = ?, session_date = ?, reason = ? "
                   + "WHERE medical_id = ?";
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                setNullableString(ps, 1, m.getDocumentPath());
                ps.setDate(2, Date.valueOf(m.getSessionDate()));
                setNullableString(ps, 3, m.getReason());
                ps.setInt(4, m.getMedicalId());
                ps.executeUpdate();
                relink(con, m.getMedicalId(), m.getStuId(), m.getSessionDate());
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    @Override
    public void updateDecision(int medicalId, ApprovalStatus status, int approvedBy) throws SQLException {
        String sql = "UPDATE medical_record SET approval_status = ?, approved_by = ? WHERE medical_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt(2, approvedBy);
            ps.setInt(3, medicalId);
            ps.executeUpdate();
        }
    }

    @Override
    public void delete(int medicalId) throws SQLException {
        // attendance.medical_id is ON DELETE SET NULL, so the absences simply become "no medical"
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM medical_record WHERE medical_id = ?")) {
            ps.setInt(1, medicalId);
            ps.executeUpdate();
        }
    }

    @Override
    public boolean existsForStudentOnDate(int stuId, LocalDate date, Integer exceptMedicalId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM medical_record WHERE stu_id = ? AND session_date = ? "
                   + "AND medical_id <> ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, stuId);
            ps.setDate(2, Date.valueOf(date));
            ps.setInt(3, exceptMedicalId == null ? -1 : exceptMedicalId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    @Override
    public int countByStatus(ApprovalStatus status) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT COUNT(*) FROM medical_record WHERE approval_status = ?")) {
            ps.setString(1, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    // ------------------------------------------------------------------ helpers

    /** Points the student's ABSENT rows of that date at this medical (FR-36). */
    private void relink(Connection con, int medicalId, int stuId, LocalDate date) throws SQLException {
        try (PreparedStatement clear = con.prepareStatement(
                "UPDATE attendance SET medical_id = NULL WHERE medical_id = ?")) {
            clear.setInt(1, medicalId);
            clear.executeUpdate();
        }
        try (PreparedStatement link = con.prepareStatement(
                "UPDATE attendance SET medical_id = ? WHERE stu_id = ? AND session_date = ? AND status = 'ABSENT'")) {
            link.setInt(1, medicalId);
            link.setInt(2, stuId);
            link.setDate(3, Date.valueOf(date));
            link.executeUpdate();
        }
    }

    private static void setNullableString(PreparedStatement ps, int index, String value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.VARCHAR);
        } else {
            ps.setString(index, value);
        }
    }

    private MedicalRecord map(ResultSet rs) throws SQLException {
        MedicalRecord m = new MedicalRecord();
        m.setMedicalId(rs.getInt("medical_id"));
        m.setStuId(rs.getInt("stu_id"));
        m.setRegNumber(rs.getString("reg_number"));
        m.setStudentName(rs.getString("first_name") + " " + rs.getString("last_name"));
        m.setDocumentPath(rs.getString("document_path"));
        m.setSessionDate(rs.getDate("session_date").toLocalDate());
        m.setReason(rs.getString("reason"));
        m.setApprovalStatus(ApprovalStatus.valueOf(rs.getString("approval_status")));
        int by = rs.getInt("approved_by");
        if (!rs.wasNull()) {
            m.setApprovedBy(by);
            m.setApprovedByName(rs.getString("ap_first") + " " + rs.getString("ap_last"));
        }
        return m;
    }
}
