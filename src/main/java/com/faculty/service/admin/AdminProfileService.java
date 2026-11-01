package com.faculty.service.admin;

import com.faculty.dao.admin.AdminUserDAO;
import com.faculty.dao.admin.AdminUserDAOImpl;
import com.faculty.exception.AdminValidationException;
import com.faculty.model.User;
import com.faculty.model.admin.AdminProfileData;
import com.faculty.util.PasswordUtil;

import java.sql.SQLException;

/** The admin editing his own profile and changing his own password. */
public class AdminProfileService {

    private final AdminUserDAO userDAO = new AdminUserDAOImpl();

    /** The logged-in admin (used to fill the screen). */
    public User getCurrent() {
        return AdminSupport.requireAdmin();
    }

    public void saveProfile(AdminProfileData in) throws AdminValidationException, SQLException {
        User me = AdminSupport.requireAdmin();

        String first = AdminSupport.text(in.firstName());
        String last = AdminSupport.text(in.lastName());
        String email = AdminSupport.text(in.email());
        AdminSupport.require(!first.isEmpty() && !last.isEmpty(), "First and last name are required.");
        AdminSupport.require(AdminSupport.validEmail(email), "Enter a valid email address.");

        AdminProfileData clean = new AdminProfileData(first, last, email,
                AdminSupport.blankToNull(in.phone()), AdminSupport.blankToNull(in.nic()), in.dob(),
                AdminSupport.blankToNull(in.street()), AdminSupport.blankToNull(in.city()),
                AdminSupport.blankToNull(in.postalCode()), AdminSupport.blankToNull(in.country()),
                AdminSupport.blankToNull(in.picturePath()));
        userDAO.updateProfile(me.getUserId(), clean);

        // keep the logged-in session in step with the database
        me.setFirstName(first);
        me.setLastName(last);
        me.setEmail(email);
        me.setPhone(clean.phone());
        me.setNic(clean.nic());
        me.setDob(clean.dob());
        me.setStreet(clean.street());
        me.setCity(clean.city());
        me.setPostalCode(clean.postalCode());
        me.setCountry(clean.country());
        me.setProfilePicturePath(clean.picturePath());
    }

    public void changePassword(String current, String newPassword, String confirm)
            throws AdminValidationException, SQLException {
        User me = AdminSupport.requireAdmin();
        AdminSupport.require(current != null && !current.isEmpty(), "Enter your current password.");
        AdminSupport.require(newPassword != null && newPassword.length() >= 6, "New password must be at least 6 characters.");
        AdminSupport.require(newPassword.equals(confirm), "New password and confirmation do not match.");

        String stored = userDAO.findPasswordHash(me.getUserId());
        AdminSupport.require(stored != null && PasswordUtil.verify(current, stored), "Current password is incorrect.");
        userDAO.updatePassword(me.getUserId(), PasswordUtil.hash(newPassword));
    }
}
