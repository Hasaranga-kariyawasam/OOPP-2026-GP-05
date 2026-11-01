package com.faculty.dao.admin;

import com.faculty.model.admin.AdminTimetableSlot;

import java.sql.SQLException;
import java.sql.Time;
import java.util.List;

public class AdminTimetableDAOImpl implements AdminTimetableDAO {

    private static final String FIND_ALL =
        "SELECT t.timetable_id, t.course_id, t.dep_id, c.course_code, d.department_name, t.day, t.start_time, "
      + "       t.end_time, t.session_type, COALESCE(t.venue,'') AS venue "
      + "FROM timetable t JOIN course c ON c.course_id = t.course_id "
      + "JOIN department d ON d.dep_id = t.dep_id "
      + "ORDER BY FIELD(t.day,'MON','TUE','WED','THU','FRI','SAT','SUN'), t.start_time";

    @Override
    public List<AdminTimetableSlot> findAll() throws SQLException {
        return Jdbc.query(FIND_ALL, rs -> new AdminTimetableSlot(rs.getInt("timetable_id"), rs.getInt("course_id"),
                rs.getInt("dep_id"), rs.getString("course_code"), rs.getString("department_name"), rs.getString("day"),
                rs.getTime("start_time").toLocalTime(), rs.getTime("end_time").toLocalTime(),
                rs.getString("session_type"), rs.getString("venue")));
    }

    @Override
    public void insert(AdminTimetableSlot s, Integer createdByAdminId) throws SQLException {
        Jdbc.update("INSERT INTO timetable (course_id, dep_id, day, start_time, end_time, session_type, venue, created_by) "
                  + "VALUES (?,?,?,?,?,?,?,?)",
                s.courseId(), s.depId(), s.day(), Time.valueOf(s.startTime()), Time.valueOf(s.endTime()),
                s.sessionType(), s.venue(), createdByAdminId);
    }

    @Override
    public void update(AdminTimetableSlot s) throws SQLException {
        Jdbc.update("UPDATE timetable SET course_id=?, dep_id=?, day=?, start_time=?, end_time=?, session_type=?, venue=? "
                  + "WHERE timetable_id=?",
                s.courseId(), s.depId(), s.day(), Time.valueOf(s.startTime()), Time.valueOf(s.endTime()),
                s.sessionType(), s.venue(), s.timetableId());
    }

    @Override
    public void delete(int timetableId) throws SQLException {
        Jdbc.update("DELETE FROM timetable WHERE timetable_id=?", timetableId);
    }
}
