package com.faculty.dao.to;

import com.faculty.config.DBConnection;
import com.faculty.model.to.SessionType;
import com.faculty.model.to.TimetableEntry;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TimetableQueryDAOImpl implements TimetableQueryDAO {

    @Override
    public List<TimetableEntry> findByDepartment(int depId) throws SQLException {
        String sql = "SELECT t.timetable_id, t.course_id, c.course_code, c.course_name, t.day, "
                   + "       t.start_time, t.end_time, t.session_type, t.venue, "
                   + "       l.first_name, l.last_name "
                   + "FROM timetable t "
                   + "JOIN course c ON c.course_id = t.course_id "
                   + "LEFT JOIN users l ON l.user_id = c.lecture_id "
                   + "WHERE t.dep_id = ? "
                   + "ORDER BY FIELD(t.day, 'MON','TUE','WED','THU','FRI','SAT','SUN'), t.start_time";
        List<TimetableEntry> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, depId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    TimetableEntry t = new TimetableEntry();
                    t.setTimetableId(rs.getInt("timetable_id"));
                    t.setCourseId(rs.getInt("course_id"));
                    t.setCourseCode(rs.getString("course_code"));
                    t.setCourseName(rs.getString("course_name"));
                    t.setDay(rs.getString("day"));
                    t.setStartTime(rs.getTime("start_time").toLocalTime());
                    t.setEndTime(rs.getTime("end_time").toLocalTime());
                    t.setSessionType(SessionType.valueOf(rs.getString("session_type")));
                    t.setVenue(rs.getString("venue"));
                    String first = rs.getString("first_name");
                    t.setLecturerName(first == null ? "-" : first + " " + rs.getString("last_name"));
                    list.add(t);
                }
            }
        }
        return list;
    }
}
