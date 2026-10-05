package com.faculty.model;

import java.time.LocalDate;

public class Undergraduate extends User {

    private String regNumber;
    private LocalDate intakeDate;
    private String status;   // REGULAR, REPEAT, BATCH_MISSED
    private String batch;
    private int depId;

    public String getRegNumber() { return regNumber; }
    public void setRegNumber(String regNumber) { this.regNumber = regNumber; }
    public LocalDate getIntakeDate() { return intakeDate; }
    public void setIntakeDate(LocalDate intakeDate) { this.intakeDate = intakeDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getBatch() { return batch; }
    public void setBatch(String batch) { this.batch = batch; }
    public int getDepId() { return depId; }
    public void setDepId(int depId) { this.depId = depId; }

    @Override
    public String getDashboardFxml() {
        return "/fxml/student_dashboard.fxml";
    }

    @Override
    public String getDashboardTitle() {
        return "Student Dashboard";
    }
}
