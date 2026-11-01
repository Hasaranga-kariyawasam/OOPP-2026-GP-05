package com.faculty;

import com.faculty.config.DBConnection;
import com.faculty.model.*;
import com.faculty.model.student.Undergraduate;
import com.faculty.service.*;
import com.faculty.exception.*;
import java.time.*;
import java.util.*;

/**
 * Console check of the Technical Officer services against db/seed_data.sql
 * (attendance scenarios, medical approval, validations).
 * Run on a freshly seeded database:  mvn compile exec:java -Dexec.mainClass=com.faculty.TOServiceCheck
 * It adds one test medical and removes it again at the end.
 */
public class TOServiceCheck {
    static int fails = 0;
    static void check(String name, boolean ok) { System.out.println((ok ? "PASS " : "FAIL ") + name); if (!ok) fails++; }

    public static void main(String[] a) throws Exception {
        try {
            runChecks();
        } finally {
            try (java.sql.Connection con = DBConnection.getConnection();
                 java.sql.PreparedStatement ps = con.prepareStatement("DELETE FROM medical_record WHERE reason = 'Fever - test'")) {
                ps.executeUpdate();
            }
        }
    }

    static void runChecks() throws Exception {
        AttendanceService att = new AttendanceService();
        MedicalService med = new MedicalService();
        List<Course> courses = att.getCourses();
        check("9 courses", courses.size() == 9);
        Course dsa = courses.stream().filter(c -> c.getCourseCode().equals("ICT2113")).findFirst().get();

        // --- batch summary: the 5 FR-34 scenarios
        List<AttendanceSummary> combined = att.getBatchSummary(dsa, null, null);
        check("24 students in batch summary", combined.size() == 24);
        Map<String, AttendanceSummary> byReg = new HashMap<>();
        for (AttendanceSummary s : combined) byReg.put(s.getRegNumber(), s);
        AttendanceSummary s1 = byReg.get("TG/2024/1001");
        check("student1 >80% scenario", s1.getScenario() == AttendanceCalculator.Scenario.MORE_THAN_80 && s1.getRawPercentage() > 80);
        AttendanceSummary s7 = byReg.get("TG/2024/1007");
        check("student7 exactly 80% (" + s7.getRawPercentage() + ")", s7.getScenario() == AttendanceCalculator.Scenario.EXACTLY_80 && s7.isEligible());
        AttendanceSummary s10 = byReg.get("TG/2024/1010");
        check("student10 <80% no medical, not eligible", s10.getScenario() == AttendanceCalculator.Scenario.LESS_THAN_80_NO_MEDICAL && !s10.isEligible());
        AttendanceSummary s13 = byReg.get("TG/2024/1013");
        check("student13 >80% with medical", s13.getScenario() == AttendanceCalculator.Scenario.MORE_THAN_80_WITH_MEDICAL);
        AttendanceSummary s15 = byReg.get("TG/2024/1015");
        System.out.printf("   student15 raw=%.1f adj=%.1f scenario=%s eligible=%s%n", s15.getRawPercentage(), s15.getAdjustedPercentage(), s15.getScenario(), s15.isEligible());
        check("student15 <80% raw but eligible with approved medicals", s15.getScenario() == AttendanceCalculator.Scenario.LESS_THAN_80_WITH_MEDICAL && s15.isEligible());
        AttendanceSummary s17 = byReg.get("TG/2024/1017");
        System.out.printf("   student17 raw=%.1f adj=%.1f scenario=%s eligible=%s%n", s17.getRawPercentage(), s17.getAdjustedPercentage(), s17.getScenario(), s17.isEligible());
        check("student17 <80% with medicals, not eligible", s17.getScenario() == AttendanceCalculator.Scenario.LESS_THAN_80_WITH_MEDICAL && !s17.isEligible());
        AttendanceSummary s19 = byReg.get("TG/2023/1004");
        System.out.printf("   student19 raw=%.1f adj=%.1f eligible=%s%n", s19.getRawPercentage(), s19.getAdjustedPercentage(), s19.isEligible());
        check("student19 80% with approved medical eligible", s19.isEligible());
        AttendanceSummary s24 = byReg.get("TG/2022/1023");
        System.out.printf("   student24 raw=%.1f adj=%.1f eligible=%s%n", s24.getRawPercentage(), s24.getAdjustedPercentage(), s24.isEligible());

        // theory only / practical only for a BOTH course
        List<AttendanceSummary> th = att.getBatchSummary(dsa, SessionType.THEORY, null);
        List<AttendanceSummary> pr = att.getBatchSummary(dsa, SessionType.PRACTICAL, null);
        check("theory sessions = 15", th.get(0).getTotalSessions() == 15);
        check("practical sessions = 15", pr.get(0).getTotalSessions() == 15);
        check("combined sessions = 30", combined.get(0).getTotalSessions() == 30);
        List<AttendanceSummary> batch2024 = att.getBatchSummary(dsa, null, "2024");
        System.out.println("   batches: " + att.getBatches() + "  2024 count=" + batch2024.size());

        // individual
        List<Undergraduate> students = att.getStudents();
        check("24 students", students.size() == 24);
        Undergraduate st15 = students.stream().filter(s -> s.getRegNumber().equals("TG/2024/1015")).findFirst().get();
        List<StudentCourseAttendance> ind = att.getStudentSummary(st15);
        check("student15 has 9 courses", ind.size() == 9);
        System.out.println("   student15 course1: " + ind.get(0).getCourse() + " comb=" + ind.get(0).getCombined().getRawPercentage());
        List<Attendance> log = att.getSessionLog(st15, dsa);
        check("session log 30 rows", log.size() == 30);

        // --- record a NEW session (session 16 isn't allowed -> use validation checks)
        int toId = 9;
        LocalDate today = LocalDate.now();
        List<AttendanceSheetRow> sheet = att.loadSheet(dsa, SessionType.THEORY, today);
        check("sheet 24 rows, none saved", sheet.size() == 24 && sheet.stream().noneMatch(AttendanceSheetRow::isAlreadySaved));
        try { att.saveSheet(toId, dsa, SessionType.THEORY, 3, today, sheet); check("session no clash rejected", false); }
        catch (ValidationException e) { check("session no clash rejected: " + e.getMessage(), true); }
        try { att.saveSheet(toId, dsa, SessionType.THEORY, 3, today.plusDays(2), sheet); check("future date rejected", false); }
        catch (ValidationException e) { check("future date rejected", true); }
        Course eng = courses.stream().filter(c -> c.getCourseCode().equals("ENG2122")).findFirst().get();
        try { att.saveSheet(toId, eng, SessionType.PRACTICAL, 1, today, sheet); check("no practical rejected", false); }
        catch (ValidationException e) { check("no practical component rejected: " + e.getMessage(), true); }

        // edit an existing session: flip student 1 for DSA theory session 1 and back
        Map<Integer, LocalDate> rec = att.getRecordedSessions(dsa, SessionType.THEORY);
        LocalDate s1date = rec.get(1);
        List<AttendanceSheetRow> existing = att.loadSheet(dsa, SessionType.THEORY, s1date);
        check("existing sheet loaded as saved", existing.stream().allMatch(AttendanceSheetRow::isAlreadySaved));
        AttendanceSheetRow r1 = existing.stream().filter(r -> r.getRegNumber().equals("TG/2024/1001")).findFirst().get();
        AttendanceStatus orig = r1.getStatus();
        r1.setStatus(AttendanceStatus.ABSENT);
        att.saveSheet(toId, dsa, SessionType.THEORY, 1, s1date, existing);
        AttendanceSummary after = att.getBatchSummary(dsa, SessionType.THEORY, null).stream().filter(s -> s.getRegNumber().equals("TG/2024/1001")).findFirst().get();
        check("edit lowered presence to 14/15", after.getPresentSessions() == 14);
        r1.setStatus(orig);
        att.saveSheet(toId, dsa, SessionType.THEORY, 1, s1date, existing);
        after = att.getBatchSummary(dsa, SessionType.THEORY, null).stream().filter(s -> s.getRegNumber().equals("TG/2024/1001")).findFirst().get();
        check("edit restored 15/15", after.getPresentSessions() == 15);

        // --- medical flow
        Undergraduate st10 = students.stream().filter(s -> s.getRegNumber().equals("TG/2024/1010")).findFirst().get();
        // find an ABSENT date of student10 in DSA theory
        Attendance absent = att.getSessionLog(st10, dsa).stream().filter(x -> !x.isPresent()).findFirst().get();
        System.out.println("   student10 absent on " + absent.getSessionDate() + " type " + absent.getSessionType());
        int pendingBefore = med.countPending();
        try { med.add(st10, today.plusDays(1), "x", null); check("future medical rejected", false); } catch (ValidationException e) { check("future medical rejected", true); }
        try { med.add(st10, absent.getSessionDate(), "  ", null); check("blank reason rejected", false); } catch (ValidationException e) { check("blank reason rejected", true); }
        med.add(st10, absent.getSessionDate(), "Fever - test", null);
        check("pending +1", med.countPending() == pendingBefore + 1);
        try { med.add(st10, absent.getSessionDate(), "dup", null); check("duplicate rejected", false); } catch (ValidationException e) { check("duplicate medical rejected", true); }
        MedicalRecord created = med.find(ApprovalStatus.PENDING, "Fever - test").get(0);
        check("absent row linked to medical", att.getSessionLog(st10, dsa).stream().anyMatch(x -> x.getAttendanceId() == absent.getAttendanceId() && created.getMedicalId() == x.getMedicalId()));
        AttendanceSummary before10 = att.getBatchSummary(dsa, null, null).stream().filter(s -> s.getRegNumber().equals("TG/2024/1010")).findFirst().get();
        check("pending medical does NOT excuse absence", before10.getExcusedHours() == 0 && before10.getMedicalSessions() >= 1);
        med.decide(created, ApprovalStatus.APPROVED, toId);
        AttendanceSummary after10 = att.getBatchSummary(dsa, null, null).stream().filter(s -> s.getRegNumber().equals("TG/2024/1010")).findFirst().get();
        check("approved medical excuses absence (hours " + after10.getExcusedHours() + ")", after10.getExcusedHours() > 0 && after10.getAdjustedPercentage() > after10.getRawPercentage());
        MedicalRecord approved = med.find(ApprovalStatus.APPROVED, "Fever - test").get(0);
        try { med.update(approved, absent.getSessionDate(), "edit", null); check("edit after decision rejected", false); } catch (ValidationException e) { check("edit after decision rejected: " + e.getMessage(), true); }
        try { med.delete(approved); check("delete decided rejected", false); } catch (ValidationException e) { check("delete decided rejected", true); }
        // cleanup: use direct reject then... delete requires pending -> use SQL cleanup below
        System.out.println("CREATED_MEDICAL_ID=" + approved.getMedicalId());

        // misc services
        check("notices for TO", new NoticeService().getNotices(1).size() >= 2);
        check("timetable 13 rows", new TimetableService().getTimetable(1).size() == 13);
        TODashboardService.Stats st = new TODashboardService().loadStats(9, 1, DayOfWeek.MONDAY);
        System.out.println("   stats: " + st);
        System.out.println(fails == 0 ? "ALL PASSED" : (fails + " FAILED"));
    }
}
