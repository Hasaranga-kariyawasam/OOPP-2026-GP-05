package com.faculty.service;

/**
 * The attendance rules in ONE place (BR-09, BR-10, FR-30, FR-34, FR-35).
 * Pure maths - no database, no GUI - so other roles (lecturer eligibility, student view)
 * can reuse exactly the same rules.
 */
public final class AttendanceCalculator {

    /** Minimum attendance for the final exam (BR-10). */
    public static final double REQUIRED_PERCENTAGE = 80.0;

    /** Every theory / practical component has 15 sessions (project brief). */
    public static final int SESSIONS_PER_COMPONENT = 15;

    private static final double EPSILON = 0.0001;

    private AttendanceCalculator() { }

    /** attended / total as a percentage (0 when nothing has been recorded yet). */
    public static double percentage(double attended, double total) {
        if (total <= 0) {
            return 0.0;
        }
        return (attended * 100.0) / total;
    }

    /** True when percentage is at least 80% (floating point safe). */
    public static boolean meetsRequirement(double percentage) {
        return percentage + EPSILON >= REQUIRED_PERCENTAGE;
    }

    /** The five cases the brief asks us to be able to identify (FR-34). */
    public enum Scenario {
        MORE_THAN_80("More than 80%"),
        MORE_THAN_80_WITH_MEDICAL("More than 80% with medical"),
        EXACTLY_80("Exactly 80%"),
        LESS_THAN_80_NO_MEDICAL("Less than 80% without medical"),
        LESS_THAN_80_WITH_MEDICAL("Less than 80% with medical");

        private final String label;

        Scenario(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    /**
     * Classifies a student by the RAW percentage (before medicals) and whether
     * any medical was submitted for his / her absences.
     */
    public static Scenario scenario(double rawPercentage, boolean hasMedical) {
        if (Math.abs(rawPercentage - REQUIRED_PERCENTAGE) < EPSILON) {
            return Scenario.EXACTLY_80;
        }
        if (rawPercentage > REQUIRED_PERCENTAGE) {
            return hasMedical ? Scenario.MORE_THAN_80_WITH_MEDICAL : Scenario.MORE_THAN_80;
        }
        return hasMedical ? Scenario.LESS_THAN_80_WITH_MEDICAL : Scenario.LESS_THAN_80_NO_MEDICAL;
    }
}
