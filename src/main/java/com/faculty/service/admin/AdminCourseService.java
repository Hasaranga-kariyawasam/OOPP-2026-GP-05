package com.faculty.service.admin;

import com.faculty.dao.admin.AdminCourseDAO;
import com.faculty.dao.admin.AdminCourseDAOImpl;
import com.faculty.exception.AdminValidationException;
import com.faculty.model.admin.AdminCourse;
import com.faculty.model.admin.AdminCourseForm;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;

/** Business rules for course management. */
public class AdminCourseService {

    private static final Set<String> TYPES = Set.of("THEORY", "PRACTICAL", "BOTH");

    private final AdminCourseDAO courseDAO = new AdminCourseDAOImpl();

    public List<AdminCourse> search(String text) throws SQLException {
        AdminSupport.requireAdmin();
        return courseDAO.search(AdminSupport.text(text));
    }

    public void add(AdminCourseForm form) throws AdminValidationException, SQLException {
        int adminId = AdminSupport.currentAdminId();
        courseDAO.insert(validate(0, form), adminId);
    }

    public void update(int courseId, AdminCourseForm form) throws AdminValidationException, SQLException {
        AdminSupport.requireAdmin();
        AdminSupport.requireSelected(courseId, "a course");
        courseDAO.update(validate(courseId, form));
    }

    public void delete(int courseId) throws AdminValidationException, SQLException {
        AdminSupport.requireAdmin();
        AdminSupport.requireSelected(courseId, "a course");
        courseDAO.delete(courseId);
    }

    private AdminCourse validate(int courseId, AdminCourseForm f) throws AdminValidationException {
        String code = AdminSupport.text(f.code()).toUpperCase();
        String name = AdminSupport.text(f.name());
        String semester = AdminSupport.text(f.semester());

        AdminSupport.require(!code.isEmpty() && code.length() <= 10, "Course code is required (max 10 characters).");
        AdminSupport.require(!name.isEmpty(), "Course name is required.");

        int theory;
        int practical;
        try {
            theory = Integer.parseInt(AdminSupport.text(f.theoryCredits()));
            practical = Integer.parseInt(AdminSupport.text(f.practicalCredits()));
        } catch (NumberFormatException e) {
            throw new AdminValidationException("Credits must be whole numbers.");
        }
        AdminSupport.require(theory >= 0 && practical >= 0 && theory + practical > 0,
                "Total credits must be more than 0.");
        AdminSupport.require(!semester.isEmpty(), "Semester is required (e.g. L2S1).");
        AdminSupport.require(f.type() != null && TYPES.contains(f.type()), "Choose a course type.");

        return new AdminCourse(courseId, code, name, theory, practical, semester, f.type(), f.lecturerId(), null);
    }
}
