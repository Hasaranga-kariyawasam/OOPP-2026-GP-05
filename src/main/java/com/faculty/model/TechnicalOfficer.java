package com.faculty.model;

public class TechnicalOfficer extends Staff {

    @Override
    public String getDashboardFxml() {
        return "/fxml/to_dashboard.fxml";
    }

    @Override
    public String getDashboardTitle() {
        return "Technical Officer Dashboard";
    }
}
