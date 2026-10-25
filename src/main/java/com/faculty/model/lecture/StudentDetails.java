package com.faculty.model.lecture;

import java.time.LocalDate;

/**
 * Read-only view of one undergraduate (users + undergraduate + department tables joined).
 * ENCAPSULATION: private fields, access through getters. The password hash is deliberately NOT here.
 */
public class StudentDetails {

    private int userId;
    private String regNumber;
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
    private String departmentName;
    private String batch;
    private String status;        // REGULAR, REPEAT, BATCH_MISSED
    private LocalDate intakeDate;
    private boolean active;

    public String getFullName() { return (firstName + " " + lastName).trim(); }

    /** Street, city, postal code and country joined together, skipping empty parts. */
    public String getAddress() {
        StringBuilder sb = new StringBuilder();
        for (String part : new String[]{street, city, postalCode, country}) {
            if (part != null && !part.isBlank()) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(part.trim());
            }
        }
        return sb.toString();
    }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getRegNumber() { return regNumber; }
    public void setRegNumber(String regNumber) { this.regNumber = regNumber; }
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
    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }
    public String getBatch() { return batch; }
    public void setBatch(String batch) { this.batch = batch; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDate getIntakeDate() { return intakeDate; }
    public void setIntakeDate(LocalDate intakeDate) { this.intakeDate = intakeDate; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
