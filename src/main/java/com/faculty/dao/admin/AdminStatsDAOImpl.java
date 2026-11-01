package com.faculty.dao.admin;

import com.faculty.model.admin.DashboardStats;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class AdminStatsDAOImpl implements AdminStatsDAO {

    // credit-weighted GPA of every student that has grades
    private static final String GPA_PER_STUDENT =
        "(SELECT SUM(g.grade_point * (c.credits_theory + c.credits_practical)) "
      + "/ NULLIF(SUM(c.credits_theory + c.credits_practical), 0) AS gpa "
      + "FROM grade g JOIN course c ON c.course_id = g.course_id GROUP BY g.stu_id) t";

    @Override
    public DashboardStats load() throws SQLException {
        return new DashboardStats(
                count("SELECT COUNT(*) FROM undergraduate"),
                count("SELECT COUNT(*) FROM course"),
                count("SELECT COUNT(*) FROM users WHERE role IN ('LECTURER','TECHNICAL_OFFICER')"),
                count("SELECT COUNT(*) FROM undergraduate WHERE status = 'REGULAR'"),
                count("SELECT COUNT(*) FROM undergraduate WHERE status = 'REPEAT'"),
                count("SELECT COUNT(*) FROM undergraduate WHERE status = 'BATCH_MISSED'"),
                decimal("SELECT ROUND(AVG(gpa), 2) FROM " + GPA_PER_STUDENT),
                decimal("SELECT ROUND(MAX(gpa), 2) FROM " + GPA_PER_STUDENT),
                count("SELECT COUNT(*) FROM " + GPA_PER_STUDENT + " WHERE gpa < 2.0"),
                decimal("SELECT ROUND(100 * SUM(status = 'PRESENT') / COUNT(*), 1) FROM attendance"),
                count("SELECT COUNT(*) FROM medical_record WHERE approval_status = 'PENDING'"),
                count("SELECT COUNT(*) FROM notice"));
    }

    private int count(String sql) throws SQLException {
        List<Integer> rows = Jdbc.query(sql, rs -> rs.getInt(1));
        return rows.isEmpty() ? 0 : rows.get(0);
    }

    /** First column of the first row; null when there is no data. */
    private Double decimal(String sql) throws SQLException {
        List<Double> rows = Jdbc.query(sql, rs -> {
            BigDecimal b = rs.getBigDecimal(1);
            return b == null ? null : b.doubleValue();
        });
        return rows.isEmpty() ? null : rows.get(0);
    }
}
