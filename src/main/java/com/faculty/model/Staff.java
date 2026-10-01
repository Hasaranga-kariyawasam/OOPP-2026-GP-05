package com.faculty.model;

/** Admin, Lecturer and Technical Officer are all staff (WORKS_IN a department). */
public abstract class Staff extends User {

    private int depId;

    public int getDepId() { return depId; }
    public void setDepId(int depId) { this.depId = depId; }
}
