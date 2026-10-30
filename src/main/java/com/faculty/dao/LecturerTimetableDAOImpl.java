package com.faculty.dao;

import com.faculty.config.DBConnection;
import com.faculty.model.lecture.LecturerTimetableEntry;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LecturerTimetableDAOImpl implements LecturerTimetableDAO {

    private static final String FIND_BY_LECTURER =
        "SELECT t.day, t.start_time, t.end_time, c.course_code, c.course_name, t.session_type, t.venue "
      + "FROM timetable t JOIN course c ON c.course_id = t.course_id "
      + "WHERE c.lecture_id = ? "
      + "ORDER BY FIELD(t.day, 'MON','TUE','WED','THU','FRI','SAT','SUN'), t.start_time";

    @Override
    public List<LecturerTimetableEntry> findByLecturer(int lecturerId) throws SQLException {
        List<LecturerTimetableEntry> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(FIND_BY_LECTURER)) {
            ps.setInt(1, lecturerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LecturerTimetableEntry e = new LecturerTimetableEntry();
                    e.setDay(rs.getString("day"));
                    e.setStartTime(rs.getTime("start_time").toLocalTime());
                    e.setEndTime(rs.getTime("end_time").toLocalTime());
                    e.setCourseCode(rs.getString("course_code"));
                    e.setCourseName(rs.getString("course_name"));
                    e.setSessionType(rs.getString("session_type"));
                    e.setVenue(rs.getString("venue"));
                    list.add(e);
                }
            }
        }
        return list;
    }
}
