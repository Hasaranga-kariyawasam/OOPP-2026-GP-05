package com.faculty.service.admin;

import com.faculty.dao.admin.AdminStaffDAO;
import com.faculty.dao.admin.AdminStaffDAOImpl;
import com.faculty.dao.admin.AdminUserDAO;
import com.faculty.dao.admin.AdminUserDAOImpl;
import com.faculty.exception.AdminValidationException;
import com.faculty.model.admin.AdminStaff;
import com.faculty.util.PasswordUtil;

import java.sql.SQLException;
import java.util.List;

/** Business rules for managing lecturers and technical officers (role = LECTURER or TECHNICAL_OFFICER). */
public class AdminStaffService {

    private final AdminStaffDAO staffDAO = new AdminStaffDAOImpl();
    private final AdminUserDAO userDAO = new AdminUserDAOImpl();

    public List<AdminStaff> search(String role, String text) throws SQLException {
        AdminSupport.requireAdmin();
        checkRole(role);
        return staffDAO.search(role, AdminSupport.text(text));
    }

    public void register(String role, AdminStaff input, String password) throws AdminValidationException, SQLException {
        AdminSupport.requireAdmin();
        checkRole(role);
        AdminStaff s = validate(input);
        AdminSupport.checkPassword(password, true);
        staffDAO.insert(role, s, PasswordUtil.hash(password));
    }

    /** A blank password keeps the current one. */
    public void update(String role, AdminStaff input, String password) throws AdminValidationException, SQLException {
        AdminSupport.requireAdmin();
        checkRole(role);
        AdminSupport.requireSelected(input.userId(), "a person");
        AdminStaff s = validate(input);
        AdminSupport.checkPassword(password, false);
        String hash = (password == null || password.isEmpty()) ? null : PasswordUtil.hash(password);
        staffDAO.update(role, s, hash);
    }

    public void delete(int userId) throws AdminValidationException, SQLException {
        AdminSupport.requireAdmin();
        AdminSupport.requireSelected(userId, "a person");
        userDAO.delete(userId);
    }

    private void checkRole(String role) {
        if (!"LECTURER".equals(role) && !"TECHNICAL_OFFICER".equals(role)) {
            throw new IllegalArgumentException("Unknown staff role: " + role);
        }
    }

    private AdminStaff validate(AdminStaff in) throws AdminValidationException {
        String first = AdminSupport.text(in.firstName());
        String last = AdminSupport.text(in.lastName());
        String username = AdminSupport.text(in.username());
        String email = AdminSupport.text(in.email());

        AdminSupport.require(!first.isEmpty() && !last.isEmpty(), "First and last name are required.");
        AdminSupport.require(!username.isEmpty(), "Username is required.");
        AdminSupport.require(AdminSupport.validEmail(email), "Enter a valid email address.");
        AdminSupport.require(in.depId() > 0, "Choose a department.");

        return new AdminStaff(in.userId(), username, first, last, email, in.depId(),
                in.departmentName(), AdminSupport.text(in.designation()));
    }
}
