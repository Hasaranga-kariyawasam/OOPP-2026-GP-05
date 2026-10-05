package com.faculty.model.admin;

import com.faculty.model.Staff;

public class Admin extends Staff {

    @Override
    public String getDashboardFxml() {
        return "/fxml/admin/admin_dashboard.fxml";
    }

    @Override
    public String getDashboardTitle() {
        return "Admin Dashboard";
    }
}
