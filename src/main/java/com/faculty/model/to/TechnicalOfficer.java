package com.faculty.model.to;

import com.faculty.model.Staff;

public class TechnicalOfficer extends Staff {

    @Override
    public String getDashboardFxml() {
        return "/fxml/to/to_dashboard.fxml";
    }

    @Override
    public String getDashboardTitle() {
        return "Technical Officer Dashboard";
    }
}
