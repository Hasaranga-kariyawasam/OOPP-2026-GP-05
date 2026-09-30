package com.faculty.model;

public class Lecturer extends Staff {

    private String designation;

    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }

    @Override
    public String getDashboardFxml() {
        return "/fxml/lecturer_dashboard.fxml";
    }

    @Override
    public String getDashboardTitle() {
        return "Lecturer Dashboard";
    }
}
