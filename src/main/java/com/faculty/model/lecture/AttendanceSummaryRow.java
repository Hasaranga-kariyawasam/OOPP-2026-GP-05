package com.faculty.model.lecture;

/**
 * Attendance of one student in one course. Hours are stored per session type so the service
 * can work out the theory, practical and combined percentages (FR-13).
 */
public class AttendanceSummaryRow {

    private int stuId;
    private String regNumber;
    private String studentName;

    private double theoryTotal, theoryPresent, theoryMedical;
    private double practicalTotal, practicalPresent, practicalMedical;

    public int getStuId() { return stuId; }
    public void setStuId(int stuId) { this.stuId = stuId; }
    public String getRegNumber() { return regNumber; }
    public void setRegNumber(String regNumber) { this.regNumber = regNumber; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public double getTheoryTotal() { return theoryTotal; }
    public void setTheoryTotal(double v) { this.theoryTotal = v; }
    public double getTheoryPresent() { return theoryPresent; }
    public void setTheoryPresent(double v) { this.theoryPresent = v; }
    public double getTheoryMedical() { return theoryMedical; }
    public void setTheoryMedical(double v) { this.theoryMedical = v; }
    public double getPracticalTotal() { return practicalTotal; }
    public void setPracticalTotal(double v) { this.practicalTotal = v; }
    public double getPracticalPresent() { return practicalPresent; }
    public void setPracticalPresent(double v) { this.practicalPresent = v; }
    public double getPracticalMedical() { return practicalMedical; }
    public void setPracticalMedical(double v) { this.practicalMedical = v; }

    // ---- derived values (percentages use hours, "medical" = absent hours covered by an APPROVED medical)

    public double getTotalHours() { return theoryTotal + practicalTotal; }
    public double getPresentHours() { return theoryPresent + practicalPresent; }
    public double getMedicalHours() { return theoryMedical + practicalMedical; }

    public static double percent(double part, double total) {
        return total <= 0 ? 0 : part * 100.0 / total;
    }

    /** Plain attendance, medical absences NOT counted. */
    public double getRawPercent() { return percent(getPresentHours(), getTotalHours()); }

    /** Attendance when approved-medical absences are counted as attended. */
    public double getEffectivePercent() { return percent(getPresentHours() + getMedicalHours(), getTotalHours()); }

    public double getTheoryPercent() { return percent(theoryPresent, theoryTotal); }
    public double getPracticalPercent() { return percent(practicalPresent, practicalTotal); }
    public boolean hasTheory() { return theoryTotal > 0; }
    public boolean hasPractical() { return practicalTotal > 0; }
    public boolean hasMedical() { return getMedicalHours() > 0; }
}
