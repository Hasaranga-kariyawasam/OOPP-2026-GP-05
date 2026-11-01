package com.faculty.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Final mark and grade rules (FR-22): final mark = 40% of the CA average + 60% of the final exam,
 * graded with the UGC scale (Circular 12-2024) that the project data uses.
 */
public final class GradeScale {

    public static final double CA_WEIGHT = 0.40;
    public static final double FINAL_WEIGHT = 0.60;

    private GradeScale() { }

    /** Final mark out of 100, rounded to 2 decimals like the grade table. */
    public static double finalMark(double caAverage, double finalExam) {
        return BigDecimal.valueOf(CA_WEIGHT * caAverage + FINAL_WEIGHT * finalExam)
                .setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    public static String gradeFor(double mark) {
        if (mark >= 85) return "A+";
        if (mark >= 75) return "A";
        if (mark >= 70) return "A-";
        if (mark >= 65) return "B+";
        if (mark >= 60) return "B";
        if (mark >= 55) return "B-";
        if (mark >= 50) return "C+";
        if (mark >= 45) return "C";
        if (mark >= 40) return "C-";
        if (mark >= 35) return "D+";
        if (mark >= 30) return "D";
        return "E";
    }

    public static double pointFor(String grade) {
        switch (grade) {
            case "A+":
            case "A":  return 4.0;
            case "A-": return 3.7;
            case "B+": return 3.3;
            case "B":  return 3.0;
            case "B-": return 2.7;
            case "C+": return 2.3;
            case "C":  return 2.0;
            case "C-": return 1.7;
            case "D+": return 1.3;
            case "D":  return 1.0;
            default:   return 0.0;
        }
    }
}
