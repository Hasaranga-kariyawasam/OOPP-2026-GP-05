package com.faculty.model;

public class Admin extends Staff {

    @Override
    public String getDashboardFxml() {
        return "/fxml/admin_dashboard.fxml";
    }

    @Override
    public String getDashboardTitle() {
        return "Admin Dashboard";
    }
}
