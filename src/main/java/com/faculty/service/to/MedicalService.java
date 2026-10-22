package com.faculty.service;

import com.faculty.dao.MedicalDAO;
import com.faculty.dao.MedicalDAOImpl;
import com.faculty.dao.StudentQueryDAO;
import com.faculty.dao.StudentQueryDAOImpl;
import com.faculty.exception.ServiceException;
import com.faculty.exception.ValidationException;
import com.faculty.model.ApprovalStatus;
import com.faculty.model.MedicalRecord;
import com.faculty.model.student.Undergraduate;
import com.faculty.util.to.FileStorageUtil;
import com.faculty.util.to.Validator;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

/** Business rules for medical records (FR-36 .. FR-38, BR-05, BR-09). */
public class MedicalService {

    private static final long MAX_DOCUMENT_BYTES = 5L * 1024 * 1024;     // 5 MB
    private static final List<String> ALLOWED_EXTENSIONS = List.of(".pdf", ".jpg", ".jpeg", ".png");

    private final MedicalDAO medicalDAO = new MedicalDAOImpl();
    private final StudentQueryDAO studentDAO = new StudentQueryDAOImpl();

    public List<Undergraduate> getStudents() throws ServiceException {
        try {
            return studentDAO.findAllActive();
        } catch (SQLException e) {
            throw ServiceSupport.wrap("load the undergraduates", e);
        }
    }

    /** status = null shows every status; search = null / blank shows everybody. */
    public List<MedicalRecord> find(ApprovalStatus status, String search) throws ServiceException {
        try {
            return medicalDAO.findAll(status, search);
        } catch (SQLException e) {
            throw ServiceSupport.wrap("load the medical records", e);
        }
    }

    public int countPending() throws ServiceException {
        try {
            return medicalDAO.countByStatus(ApprovalStatus.PENDING);
        } catch (SQLException e) {
            throw ServiceSupport.wrap("load the medical count", e);
        }
    }

    /** Adds a new medical (status PENDING). document may be null. */
    public void add(Undergraduate student, LocalDate date, String reason, File document)
            throws ValidationException, ServiceException {
        if (student == null) {
            throw new ValidationException("Please select an undergraduate.");
        }
        validateDateAndReason(date, reason);
        try {
            if (medicalDAO.existsForStudentOnDate(student.getUserId(), date, null)) {
                throw new ValidationException("This undergraduate already has a medical for that date.");
            }
            MedicalRecord m = new MedicalRecord();
            m.setStuId(student.getUserId());
            m.setSessionDate(date);
            m.setReason(Validator.optional(reason));
            m.setDocumentPath(storeDocument(document, student.getRegNumber(), date));
            medicalDAO.insert(m);
        } catch (SQLException e) {
            throw ServiceSupport.wrap("save the medical record", e);
        }
    }

    /** Changes date / reason / document. Only allowed while the medical is still PENDING (FR-37). */
    public void update(MedicalRecord existing, LocalDate date, String reason, File newDocument)
            throws ValidationException, ServiceException {
        if (existing == null) {
            throw new ValidationException("Please select a medical record.");
        }
        if (!existing.isEditable()) {
            throw new ValidationException("Only pending medicals can be edited.");
        }
        validateDateAndReason(date, reason);
        try {
            if (medicalDAO.existsForStudentOnDate(existing.getStuId(), date, existing.getMedicalId())) {
                throw new ValidationException("This undergraduate already has a medical for that date.");
            }
            MedicalRecord m = new MedicalRecord();
            m.setMedicalId(existing.getMedicalId());
            m.setStuId(existing.getStuId());
            m.setSessionDate(date);
            m.setReason(Validator.optional(reason));
            String stored = storeDocument(newDocument, existing.getRegNumber(), date);
            m.setDocumentPath(stored != null ? stored : existing.getDocumentPath());
            medicalDAO.update(m);
        } catch (SQLException e) {
            throw ServiceSupport.wrap("update the medical record", e);
        }
    }

    /** Approve (counts in eligibility - BR-09) or reject. */
    public void decide(MedicalRecord medical, ApprovalStatus decision, int toId)
            throws ValidationException, ServiceException {
        if (medical == null) {
            throw new ValidationException("Please select a medical record.");
        }
        if (decision != ApprovalStatus.APPROVED && decision != ApprovalStatus.REJECTED) {
            throw new ValidationException("Decision must be Approved or Not Approved.");
        }
        try {
            medicalDAO.updateDecision(medical.getMedicalId(), decision, toId);
        } catch (SQLException e) {
            throw ServiceSupport.wrap("save the decision", e);
        }
    }

    /** Only pending medicals can be deleted - decided ones stay as evidence. */
    public void delete(MedicalRecord medical) throws ValidationException, ServiceException {
        if (medical == null) {
            throw new ValidationException("Please select a medical record.");
        }
        if (!medical.isEditable()) {
            throw new ValidationException("Only pending medicals can be deleted.");
        }
        try {
            medicalDAO.delete(medical.getMedicalId());
        } catch (SQLException e) {
            throw ServiceSupport.wrap("delete the medical record", e);
        }
    }

    // ------------------------------------------------------------------ helpers

    private void validateDateAndReason(LocalDate date, String reason) throws ValidationException {
        if (date == null) {
            throw new ValidationException("Please select the date the medical covers.");
        }
        if (date.isAfter(LocalDate.now())) {
            throw new ValidationException("The medical date cannot be in the future.");
        }
        Validator.required("Reason", reason);
        Validator.maxLength("Reason", reason, 255);
    }

    /** Copies the chosen file into uploads/medicals; returns the stored path or null when no file was chosen. */
    private String storeDocument(File document, String regNumber, LocalDate date)
            throws ValidationException, ServiceException {
        if (document == null) {
            return null;
        }
        String name = document.getName().toLowerCase(Locale.ROOT);
        if (ALLOWED_EXTENSIONS.stream().noneMatch(name::endsWith)) {
            throw new ValidationException("The document must be a PDF, JPG or PNG file.");
        }
        if (document.length() > MAX_DOCUMENT_BYTES) {
            throw new ValidationException("The document must be smaller than 5 MB.");
        }
        try {
            return FileStorageUtil.store(document, "medicals", regNumber + "_" + date);
        } catch (IOException e) {
            System.err.println("[FILE] medical document copy failed: " + e.getMessage());
            throw new ServiceException("Could not save the medical document. Please try again.", e);
        }
    }
}
