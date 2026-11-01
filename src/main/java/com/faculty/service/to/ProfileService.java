package com.faculty.service;

import com.faculty.dao.ProfileDAO;
import com.faculty.dao.ProfileDAOImpl;
import com.faculty.dao.UserDAO;
import com.faculty.dao.UserDAOImpl;
import com.faculty.exception.ServiceException;
import com.faculty.exception.ValidationException;
import com.faculty.model.ProfileUpdate;
import com.faculty.model.User;
import com.faculty.util.to.FileStorageUtil;
import com.faculty.util.to.Validator;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDate;
import java.util.Locale;

/** "Edit my profile" - everything except username and password (FR-13, BR-02). */
public class ProfileService {

    private static final long MAX_PICTURE_BYTES = 2L * 1024 * 1024;     // 2 MB

    private final ProfileDAO profileDAO = new ProfileDAOImpl();
    private final UserDAO userDAO = new UserDAOImpl();

    /**
     * Validates and saves the profile, then returns the freshly loaded user
     * (the caller puts it back into the SessionManager).
     *
     * @param newPicture image chosen by the user, or null to keep the current picture
     */
    public User updateProfile(User current, ProfileUpdate input, File newPicture)
            throws ValidationException, ServiceException {

        ProfileUpdate clean = validate(input);

        String picturePath = current.getProfilePicturePath();
        if (newPicture != null) {
            picturePath = storePicture(current, newPicture);
        }

        try {
            profileDAO.updateOwnProfile(current.getUserId(), clean, picturePath);
            return userDAO.findByUsername(current.getUsername());
        } catch (SQLIntegrityConstraintViolationException e) {
            String msg = String.valueOf(e.getMessage()).toLowerCase(Locale.ROOT);
            if (msg.contains("nic")) {
                throw new ValidationException("This NIC number is already used by another account.");
            }
            throw new ValidationException("This e-mail address is already used by another account.");
        } catch (SQLException e) {
            throw ServiceSupport.wrap("save your profile", e);
        }
    }

    private ProfileUpdate validate(ProfileUpdate in) throws ValidationException {
        String first = Validator.required("First name", in.firstName());
        String last = Validator.required("Last name", in.lastName());
        String email = Validator.required("E-mail", in.email());
        Validator.email(email);
        Validator.maxLength("First name", first, 50);
        Validator.maxLength("Last name", last, 50);
        Validator.maxLength("E-mail", email, 100);

        String nic = Validator.optional(in.nic());
        Validator.nic(nic);
        String phone = Validator.optional(in.phone());
        Validator.phone(phone);

        LocalDate dob = in.dob();
        if (dob != null && (dob.isAfter(LocalDate.now().minusYears(18)) || dob.getYear() < 1940)) {
            throw new ValidationException("Date of birth is not valid (staff must be at least 18 years old).");
        }

        String street = Validator.optional(in.street());
        String city = Validator.optional(in.city());
        String postal = Validator.optional(in.postalCode());
        String country = Validator.optional(in.country());
        Validator.maxLength("Street", street, 100);
        Validator.maxLength("City", city, 50);
        Validator.maxLength("Postal code", postal, 10);
        Validator.maxLength("Country", country, 50);

        return new ProfileUpdate(first, last, email, nic, dob, phone, street, city, postal, country);
    }

    private String storePicture(User user, File picture) throws ValidationException, ServiceException {
        String name = picture.getName().toLowerCase(Locale.ROOT);
        if (!(name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg"))) {
            throw new ValidationException("The profile picture must be a PNG or JPG image.");
        }
        if (picture.length() > MAX_PICTURE_BYTES) {
            throw new ValidationException("The profile picture must be smaller than 2 MB.");
        }
        try {
            return FileStorageUtil.store(picture, "profiles", user.getUsername());
        } catch (IOException e) {
            System.err.println("[FILE] profile picture copy failed: " + e.getMessage());
            throw new ServiceException("Could not save the profile picture. Please try again.", e);
        }
    }
}
