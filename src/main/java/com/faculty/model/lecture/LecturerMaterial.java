package com.faculty.model.lecture;

/** One row of the course_material table: a title and the URL (stored in file_path) of the material. */
public class LecturerMaterial {

    private int materialId;
    private String title;
    private String filePath;     // the URL pasted by the lecturer
    private int courseId;

    public LecturerMaterial() { }

    public LecturerMaterial(int materialId, String title, String filePath, int courseId) {
        this.materialId = materialId;
        this.title = title;
        this.filePath = filePath;
        this.courseId = courseId;
    }

    public int getMaterialId() { return materialId; }
    public void setMaterialId(int materialId) { this.materialId = materialId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public int getCourseId() { return courseId; }
    public void setCourseId(int courseId) { this.courseId = courseId; }
}
