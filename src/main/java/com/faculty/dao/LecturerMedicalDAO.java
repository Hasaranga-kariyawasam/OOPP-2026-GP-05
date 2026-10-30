package com.faculty.dao;

import com.faculty.model.lecture.MedicalEntry;

import java.sql.SQLException;
import java.util.List;

/** ABSTRACTION: medical queries for the lecturer module (view only, FR-17). */
public interface LecturerMedicalDAO {

    /** Medicals of students enrolled in any course taught by this lecturer, newest first. */
    List<MedicalEntry> findForLecturer(int lecturerId) throws SQLException;
}
