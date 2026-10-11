package com.faculty.dao.to;

import com.faculty.model.to.ApprovalStatus;
import com.faculty.model.to.MedicalRecord;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/** ABSTRACTION: medical record storage (FR-36 .. FR-38). */
public interface MedicalDAO {

    /** All medicals, newest first. status and search (reg no / name / reason) may be null. */
    List<MedicalRecord> findAll(ApprovalStatus status, String search) throws SQLException;

    MedicalRecord findById(int medicalId) throws SQLException;

    /** Inserts the medical, links it to the student's absences on that date, returns the new id. */
    int insert(MedicalRecord medical) throws SQLException;

    /** Updates date / reason / document and re-links the absences. */
    void update(MedicalRecord medical) throws SQLException;

    /** Approve or reject (approvedBy = Technical Officer who decided). */
    void updateDecision(int medicalId, ApprovalStatus status, int approvedBy) throws SQLException;

    void delete(int medicalId) throws SQLException;

    /** True when the student already has a medical for that date (exceptMedicalId = ignore this one when editing). */
    boolean existsForStudentOnDate(int stuId, LocalDate date, Integer exceptMedicalId) throws SQLException;

    int countByStatus(ApprovalStatus status) throws SQLException;
}
