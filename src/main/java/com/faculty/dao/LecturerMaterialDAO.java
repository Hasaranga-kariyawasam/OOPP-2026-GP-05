package com.faculty.dao;

import com.faculty.model.lecture.LecturerMaterial;

import java.sql.SQLException;
import java.util.List;

/** ABSTRACTION: course_material queries used by the lecturer module. */
public interface LecturerMaterialDAO {

    /** All materials of one course, newest first. */
    List<LecturerMaterial> findByCourse(int courseId) throws SQLException;

    /** Inserts a new material uploaded by the given lecturer. */
    void insert(String title, String url, int courseId, int lecturerId) throws SQLException;

    /**
     * Changes the title and URL of a material, but only if it belongs to the given course
     * AND that course is taught by the given lecturer.
     *
     * @return true if a row was updated
     */
    boolean update(int materialId, String title, String url, int courseId, int lecturerId) throws SQLException;
}
