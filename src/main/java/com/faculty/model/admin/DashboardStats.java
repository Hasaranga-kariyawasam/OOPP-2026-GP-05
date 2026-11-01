package com.faculty.model.admin;

/** Numbers shown on the admin dashboard. The Double values are null when there is no data yet. */
public record DashboardStats(int students, int courses, int faculty, int regular, int repeat, int batchMissed,
                             Double averageGpa, Double topGpa, int lowGpaCount, Double attendancePercent,
                             int pendingMedicals, int notices) {
}
