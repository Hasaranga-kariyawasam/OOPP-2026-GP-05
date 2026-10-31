package com.faculty.model.admin;

import java.time.LocalDate;

/** One attendance record shown in the admin attendance screen. */
public record AdminAttendanceRow(int attendanceId, String regNumber, String courseCode, String sessionType,
                                 LocalDate sessionDate, int sessionNo, String status) {
}
