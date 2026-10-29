package com.faculty.dao.student;

import com.faculty.model.student.StudentRecords.*;

import java.sql.SQLException;
import java.util.List;

/**
 * ABSTRACTION: everything the student portal needs from the database.
 * Screens and services only know this interface, not JDBC.
 * Every method is read-only except the two profile updates (students may change
 * only their contact details and picture).
 */
public interface StudentDAO {

    List<CourseInfo> findCourses(int stuId) throws SQLException;

    List<Material> findMaterials(int courseId) throws SQLException;

    List<AttendanceCount> findAttendanceCounts(int stuId) throws SQLException;

    List<SessionRow> findSessions(int stuId, int courseId) throws SQLException;

    List<MedicalInfo> findMedicals(int stuId) throws SQLException;

    List<GradeRow> findGrades(int stuId) throws SQLException;

    List<TimetableRow> findTimetable(int stuId) throws SQLException;

    List<NoticeInfo> findNotices(int depId) throws SQLException;

    /** @return false when the e-mail address already belongs to another user. */
    boolean updateContact(int userId, String email, String phone, String street,
                          String city, String postalCode, String country) throws SQLException;

    void updatePicture(int userId, String picturePath) throws SQLException;
}
