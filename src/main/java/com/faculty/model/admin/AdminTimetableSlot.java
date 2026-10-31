package com.faculty.model.admin;

import java.time.LocalTime;

/** One weekly timetable slot. timetableId 0 = not saved yet. */
public record AdminTimetableSlot(int timetableId, int courseId, int depId, String courseCode,
                                 String departmentName, String day, LocalTime startTime, LocalTime endTime,
                                 String sessionType, String venue) {
}
