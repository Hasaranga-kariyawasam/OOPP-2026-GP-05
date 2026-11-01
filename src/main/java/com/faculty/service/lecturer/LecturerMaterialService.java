package com.faculty.service;

import com.faculty.dao.LecturerCourseDAO;
import com.faculty.dao.LecturerCourseDAOImpl;
import com.faculty.dao.LecturerMaterialDAO;
import com.faculty.dao.LecturerMaterialDAOImpl;
import com.faculty.model.User;
import com.faculty.model.lecture.Lecturer;
import com.faculty.model.lecture.LecturerCourse;
import com.faculty.model.lecture.LecturerMaterial;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.List;

/**
 * Rules for managing course materials:
 *  - only a logged-in lecturer, and only for courses assigned to them (SEC-02 / SEC-07)
 *  - title and URL are validated before they reach the database (SEC-03)
 * Validation problems throw IllegalArgumentException with a message that is safe to show to the user.
 */
public class LecturerMaterialService {

    private static final int MAX_TITLE = 150;   // course_material.title VARCHAR(150)
    private static final int MAX_URL = 255;     // course_material.file_path VARCHAR(255)

    private final LecturerMaterialDAO materialDAO = new LecturerMaterialDAOImpl();
    private final LecturerCourseDAO courseDAO = new LecturerCourseDAOImpl();

    public List<LecturerMaterial> getMaterials(int courseId) throws SQLException {
        int lecturerId = requireLecturerId();
        requireOwnCourse(lecturerId, courseId);
        return materialDAO.findByCourse(courseId);
    }

    public void addMaterial(int courseId, String title, String url) throws SQLException {
        int lecturerId = requireLecturerId();
        requireOwnCourse(lecturerId, courseId);
        String cleanTitle = validateTitle(title);
        String cleanUrl = validateUrl(url);
        materialDAO.insert(cleanTitle, cleanUrl, courseId, lecturerId);
    }

    public void updateMaterial(int materialId, int courseId, String title, String url) throws SQLException {
        int lecturerId = requireLecturerId();
        requireOwnCourse(lecturerId, courseId);
        if (materialId <= 0) {
            throw new IllegalArgumentException("Click the title of a material in the table first.");
        }
        String cleanTitle = validateTitle(title);
        String cleanUrl = validateUrl(url);
        if (!materialDAO.update(materialId, cleanTitle, cleanUrl, courseId, lecturerId)) {
            throw new IllegalArgumentException("This material could not be found any more. Please refresh the list.");
        }
    }

    // ------------------------------------------------------------------ helpers

    private int requireLecturerId() {
        User user = SessionManager.getCurrentUser();
        if (!(user instanceof Lecturer)) {
            throw new SecurityException("Only a logged-in lecturer can manage course materials.");
        }
        return user.getUserId();
    }

    private void requireOwnCourse(int lecturerId, int courseId) throws SQLException {
        for (LecturerCourse c : courseDAO.findByLecturer(lecturerId)) {
            if (c.getCourseId() == courseId) {
                return;
            }
        }
        throw new SecurityException("You can only manage materials of your own courses.");
    }

    private String validateTitle(String title) {
        String t = title == null ? "" : title.trim();
        if (t.isEmpty()) {
            throw new IllegalArgumentException("Please enter a title.");
        }
        if (t.length() > MAX_TITLE) {
            throw new IllegalArgumentException("The title is too long (maximum " + MAX_TITLE + " characters).");
        }
        return t;
    }

    private String validateUrl(String url) {
        String u = url == null ? "" : url.trim();
        if (u.isEmpty()) {
            throw new IllegalArgumentException("Please paste the file URL.");
        }
        if (u.length() > MAX_URL) {
            throw new IllegalArgumentException("The URL is too long (maximum " + MAX_URL + " characters).");
        }
        try {
            URI uri = new URI(u);
            String scheme = uri.getScheme();
            boolean webScheme = "http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme);
            if (!webScheme || uri.getHost() == null) {
                throw new IllegalArgumentException("Please enter a valid URL starting with http:// or https://");
            }
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("The URL is not valid. Please check it and try again.");
        }
        return u;
    }
}
