package com.faculty.service.admin;

import com.faculty.dao.admin.AdminMarkDAO;
import com.faculty.dao.admin.AdminMarkDAOImpl;
import com.faculty.exception.AdminValidationException;
import com.faculty.model.admin.AdminMark;
import com.faculty.model.admin.AdminMarkForm;

import java.sql.SQLException;
import java.util.List;

/** Business rules for marks management: marks are out of 100. */
public class AdminMarkService {

    public static final int SEARCH_LIMIT = 500;

    private final AdminMarkDAO markDAO = new AdminMarkDAOImpl();

    public List<AdminMark> search(String text) throws SQLException {
        AdminSupport.requireAdmin();
        return markDAO.search(AdminSupport.text(text), SEARCH_LIMIT);
    }

    public void add(AdminMarkForm form) throws AdminValidationException, SQLException {
        AdminSupport.requireAdmin();
        String reg = AdminSupport.text(form.regNumber());
        AdminSupport.require(!reg.isEmpty(), "Enter the student's registration number.");
        Checked c = check(form);

        Integer stuId = markDAO.findStudentIdByReg(reg);
        AdminSupport.require(stuId != null, "No student with registration number " + reg + ".");
        markDAO.insert(stuId, form.courseId(), c.evaluation, c.category, c.value);
    }

    public void update(int markId, AdminMarkForm form) throws AdminValidationException, SQLException {
        AdminSupport.requireAdmin();
        AdminSupport.requireSelected(markId, "a mark");
        Checked c = check(form);
        markDAO.update(markId, form.courseId(), c.evaluation, c.category, c.value);
    }

    public void delete(int markId) throws AdminValidationException, SQLException {
        AdminSupport.requireAdmin();
        AdminSupport.requireSelected(markId, "a mark");
        markDAO.delete(markId);
    }

    /** The cleaned-up values of the part of the form that add and update share. */
    private record Checked(String evaluation, String category, double value) { }

    private Checked check(AdminMarkForm f) throws AdminValidationException {
        AdminSupport.require(f.courseId() > 0, "Choose a course.");
        String eval = AdminSupport.text(f.evaluationType()).toUpperCase();
        AdminSupport.require(!eval.isEmpty(), "Enter the evaluation type (QUIZ1, ASSIGNMENT, MID, FINAL ...).");
        AdminSupport.require(eval.length() <= 30, "Evaluation type is too long (30 characters max).");
        AdminSupport.require("CA".equals(f.category()) || "FINAL".equals(f.category()), "Choose CA or FINAL.");
        double value;
        try {
            value = Double.parseDouble(AdminSupport.text(f.valueText()));
        } catch (NumberFormatException e) {
            throw new AdminValidationException("Mark must be a number.");
        }
        AdminSupport.require(value >= 0 && value <= 100, "Mark must be between 0 and 100.");
        return new Checked(eval, f.category(), value);
    }
}
