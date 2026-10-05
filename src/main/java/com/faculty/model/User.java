package com.faculty.model;

import java.time.LocalDate;

/**
 * Base class for every person who can log in (row in the users table).
 * ABSTRACTION: cannot be instantiated; each role decides its own dashboard.
 * ENCAPSULATION: private fields, access through getters/setters.
 */
public abstract class User {

    private int userId;
    private String username;
    private String passwordHash;
    private UserRole role;
    private String firstName;
    private String lastName;
    private String email;
    private String nic;
    private LocalDate dob;
    private String phone;
    private String street;
    private String city;
    private String postalCode;
    private String country;
    private String profilePicturePath;
    private boolean active = true;

    /** POLYMORPHISM: every subclass returns its own dashboard screen. */
    public abstract String getDashboardFxml();

    /** POLYMORPHISM: window title for the dashboard. */
    public abstract String getDashboardTitle();

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getNic() { return nic; }
    public void setNic(String nic) { this.nic = nic; }
    public LocalDate getDob() { return dob; }
    public void setDob(LocalDate dob) { this.dob = dob; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public String getProfilePicturePath() { return profilePicturePath; }
    public void setProfilePicturePath(String p) { this.profilePicturePath = p; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
