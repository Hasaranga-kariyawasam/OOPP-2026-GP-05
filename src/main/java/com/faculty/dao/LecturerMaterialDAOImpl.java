package com.faculty.dao;

import com.faculty.config.DBConnection;
import com.faculty.model.lecture.LecturerMaterial;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LecturerMaterialDAOImpl implements LecturerMaterialDAO {

    // SEC-04: all queries use '?' placeholders.
    private static final String FIND_BY_COURSE =
        "SELECT material_id, title, file_path, course_id FROM course_material "
      + "WHERE course_id = ? ORDER BY upload_date DESC, material_id DESC";

    private static final String INSERT =
        "INSERT INTO course_material (title, file_path, course_id, lecture_id) VALUES (?, ?, ?, ?)";

    private static final String UPDATE =
        "UPDATE course_material SET title = ?, file_path = ? "
      + "WHERE material_id = ? AND course_id = ? "
      + "AND course_id IN (SELECT course_id FROM course WHERE lecture_id = ?)";

    @Override
    public List<LecturerMaterial> findByCourse(int courseId) throws SQLException {
        List<LecturerMaterial> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(FIND_BY_COURSE)) {
            ps.setInt(1, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new LecturerMaterial(
                            rs.getInt("material_id"),
                            rs.getString("title"),
                            rs.getString("file_path"),
                            rs.getInt("course_id")));
                }
            }
        }
        return list;
    }

    @Override
    public void insert(String title, String url, int courseId, int lecturerId) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(INSERT)) {
            ps.setString(1, title);
            ps.setString(2, url);
            ps.setInt(3, courseId);
            ps.setInt(4, lecturerId);
            ps.executeUpdate();
        }
    }

    @Override
    public boolean update(int materialId, String title, String url, int courseId, int lecturerId) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(UPDATE)) {
            ps.setString(1, title);
            ps.setString(2, url);
            ps.setInt(3, materialId);
            ps.setInt(4, courseId);
            ps.setInt(5, lecturerId);
            return ps.executeUpdate() > 0;
        }
    }
}
