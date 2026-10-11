package com.faculty.util.admin;

import com.faculty.config.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Tiny JDBC helpers shared by the admin screens. Every value goes through a '?' placeholder (SEC-04). */
public final class AdminDb {

    private AdminDb() { }

    /** Combo-box entry: database id + text shown to the admin. */
    public record Item(int id, String label) {
        @Override
        public String toString() {
            return label;
        }
    }

    /** Runs a SELECT and returns every row as a list of strings (NULL becomes ""). */
    public static List<List<String>> query(String sql, Object... params) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                int cols = rs.getMetaData().getColumnCount();
                List<List<String>> rows = new ArrayList<>();
                while (rs.next()) {
                    List<String> row = new ArrayList<>(cols);
                    for (int i = 1; i <= cols; i++) {
                        String v = rs.getString(i);
                        row.add(v == null ? "" : v);
                    }
                    rows.add(row);
                }
                return rows;
            }
        }
    }

    /** INSERT / UPDATE / DELETE. Returns the number of affected rows. */
    public static int update(String sql, Object... params) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            bind(ps, params);
            return ps.executeUpdate();
        }
    }

    /** SELECT id, label ... -> list of combo items. */
    public static List<Item> items(String sql, Object... params) throws SQLException {
        List<Item> list = new ArrayList<>();
        for (List<String> row : query(sql, params)) {
            list.add(new Item(Integer.parseInt(row.get(0)), row.get(1)));
        }
        return list;
    }

    public static void bind(PreparedStatement ps, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            ps.setObject(i + 1, params[i]);
        }
    }
}
