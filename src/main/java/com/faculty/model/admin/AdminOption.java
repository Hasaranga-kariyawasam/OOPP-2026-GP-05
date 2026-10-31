package com.faculty.model.admin;

/** Combo-box entry: database id + the text shown to the admin. */
public record AdminOption(int id, String label) {
    @Override
    public String toString() {
        return label;
    }
}
