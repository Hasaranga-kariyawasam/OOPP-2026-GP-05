package com.faculty.service.admin;

import com.faculty.dao.admin.AdminStudentDAO;
import com.faculty.dao.admin.AdminStudentDAOImpl;
import com.faculty.dao.admin.AdminUserDAO;
import com.faculty.dao.admin.AdminUserDAOImpl;
import com.faculty.exception.AdminValidationException;
import com.faculty.model.admin.AdminStudent;
import com.faculty.util.PasswordUtil;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;

/** Business rules for managing undergraduate accounts. */
public class AdminStudentService {

    private static final Set<String> STATUSES = Set.of("REGULAR", "REPEAT", "BATCH_MISSED");

    private final AdminStudentDAO studentDAO = new AdminStudentDAOImpl();
    private final AdminUserDAO userDAO = new AdminUserDAOImpl();

    public List<AdminStudent> search(String text) throws SQLException {
        AdminSupport.requireAdmin();
        return studentDAO.search(AdminSupport.text(text));
    }

    public void register(AdminStudent input, String password) throws AdminValidationException, SQLException {
        AdminSupport.requireAdmin();
        AdminStudent s = validate(input);
        AdminSupport.checkPassword(password, true);
        studentDAO.insert(s, PasswordUtil.hash(password));
    }

    /** A blank password keeps the current one. */
    public void update(AdminStudent input, String password) throws AdminValidationException, SQLException {
        AdminSupport.requireAdmin();
        AdminSupport.requireSelected(input.userId(), "a student");
        AdminStudent s = validate(input);
        AdminSupport.checkPassword(password, false);
        String hash = (password == null || password.isEmpty()) ? null : PasswordUtil.hash(password);
        studentDAO.update(s, hash);
    }

    public void delete(int userId) throws AdminValidationException, SQLException {
        AdminSupport.requireAdmin();
        AdminSupport.requireSelected(userId, "a student");
        userDAO.delete(userId);
    }

    /** Trims the text fields and rejects bad values. */
    private AdminStudent validate(AdminStudent in) throws AdminValidationException {
        String reg = AdminSupport.text(in.regNumber());
        String first = AdminSupport.text(in.firstName());
        String last = AdminSupport.text(in.lastName());
        String username = AdminSupport.text(in.username());
        String email = AdminSupport.text(in.email());
        String batch = AdminSupport.text(in.batch());

        AdminSupport.require(reg.matches("TG/\\d{4}/\\d{4}"), "Registration number must look like TG/2023/1753.");
        AdminSupport.require(!first.isEmpty() && !last.isEmpty(), "First and last name are required.");
        AdminSupport.require(!username.isEmpty(), "Username is required.");
        AdminSupport.require(AdminSupport.validEmail(email), "Enter a valid email address.");
        AdminSupport.require(!batch.isEmpty(), "Batch is required (e.g. 2023).");
        AdminSupport.require(in.status() != null && STATUSES.contains(in.status()), "Choose a status.");
        AdminSupport.require(in.intakeDate() != null, "Choose the intake date.");
        AdminSupport.require(in.depId() > 0, "Choose a department.");

        return new AdminStudent(in.userId(), reg, first, last, username, email, batch, in.status(),
                in.intakeDate(), in.depId(), in.departmentName());
    }
}
