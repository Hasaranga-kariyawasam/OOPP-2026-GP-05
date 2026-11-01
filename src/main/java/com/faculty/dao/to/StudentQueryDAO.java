package com.faculty.dao;

import com.faculty.model.student.Undergraduate;

import java.sql.SQLException;
import java.util.List;

/** Read-only undergraduate lookups (pick lists, batch filter). Student create / edit belongs to the Admin. */
public interface StudentQueryDAO {

    /** All active undergraduates ordered by registration number. */
    List<Undergraduate> findAllActive() throws SQLException;

    /** Distinct batch codes, e.g. 2022, 2023, 2024. */
    List<String> findBatches() throws SQLException;
}
