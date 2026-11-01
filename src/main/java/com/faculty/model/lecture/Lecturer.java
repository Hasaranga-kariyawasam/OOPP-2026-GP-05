package com.faculty.model.lecture;

import com.faculty.model.Staff;

public class Lecturer extends Staff {

    private String designation;

    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }

    @Override
    public String getDashboardFxml() {
        return "/fxml/lecture/lecturer_dashboard.fxml";
    }

    @Override
    public String getDashboardTitle() {
        return "Lecturer Dashboard";
    }
}
