package com.faculty.model.admin;

/** What the admin typed on the timetable screen (times are still text such as 08:30). */
public record AdminTimetableForm(int courseId, int depId, String day, String startText, String endText,
                                 String sessionType, String venue) {
}
