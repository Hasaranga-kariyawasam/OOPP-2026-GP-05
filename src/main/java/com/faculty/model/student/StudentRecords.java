package com.faculty.model.student;

/**
 * Small read-only data holders used by the student screens.
 * Everything the student portal shows is carried in one of these records,
 * so the controllers never touch ResultSets directly.
 */
public final class StudentRecords {

    private StudentRecords() { }

    /** One enrolled course (enrollment + course + lecturer name). */
    public record CourseInfo(int courseId, String code, String name, int creditsTheory,
                             int creditsPractical, String semester, String type,
                             String lecturer, String academicYear) {
        public int credits() {
            return creditsTheory + creditsPractical;
        }
    }

    /** Lecture material uploaded for a course. */
    public record Material(String title, String uploadDate, String filePath) { }

    /** Raw attendance counts for one course and one component (THEORY / PRACTICAL). */
    public record AttendanceCount(int courseId, String code, String name, String component,
                                  int recorded, int present, int approvedMedical) { }

    /** One line of the attendance summary table (already calculated). */
    public record AttendanceRow(int courseId, String code, String name, String component,
                                int totalSessions, int present, int absent, int approvedMedical,
                                double percent, boolean eligible) { }

    /** One attended / missed session. */
    public record SessionRow(int sessionNo, String component, String date, String status, String medical) { }

    /** One medical submitted by the student. */
    public record MedicalInfo(int id, String date, String reason, String status, String document) { }

    /** Final grade of a course. caPercent is -1 when no CA marks exist. */
    public record GradeRow(String code, String name, int credits, String semester,
                           double caPercent, double finalMark, String grade, double gradePoint) { }

    /** SGPA of one semester. */
    public record SemesterGpa(String semester, int credits, double sgpa) { }

    /** One weekly timetable slot. */
    public record TimetableRow(String day, String start, String end, String code, String name,
                               String type, String venue) { }

    /** A notice visible to students. */
    public record NoticeInfo(String title, String content, String date) { }
}
