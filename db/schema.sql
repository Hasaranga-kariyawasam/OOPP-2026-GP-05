-- =====================================================================
-- Faculty Management System - MySQL schema
-- ICT2132 Mini Project - Group 05
-- Derived from: OOPP-2026-GP-05_ER.pdf (final EER)
-- Target: MySQL 8.0+ (CHECK constraints are enforced from 8.0.16)
--
-- WARNING: this script DROPS and re-creates every table so that anyone
-- in the group can reset their local database. Do not run it on a
-- database that holds data you want to keep.
--
-- Run:  mysql -u root -p < db/schema.sql
-- =====================================================================

CREATE DATABASE IF NOT EXISTS faculty_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE faculty_db;

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS grade, marks, attendance, medical_record, enrollment,
                     course_material, timetable, notice, course,
                     admin, lecturer, technical_officer, staff,
                     undergraduate, department, users;
SET FOREIGN_KEY_CHECKS = 1;

-- ---------------------------------------------------------------------
-- DEPARTMENT
-- ---------------------------------------------------------------------
CREATE TABLE department (
    dep_id          INT AUTO_INCREMENT PRIMARY KEY,
    department_name VARCHAR(100) NOT NULL UNIQUE,
    faculty         VARCHAR(100) NOT NULL
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- USERS  (supertype - shared by every role)
-- Table is called "users" (not "user") to avoid reserved-word problems.
-- Composite attributes Name and Address are stored as flat columns.
-- ---------------------------------------------------------------------
CREATE TABLE users (
    user_id              INT AUTO_INCREMENT PRIMARY KEY,
    username             VARCHAR(50)  NOT NULL UNIQUE,
    password_hash        VARCHAR(255) NOT NULL,          -- BCrypt hash, never plain text (SEC-01)
    role                 ENUM('ADMIN','LECTURER','TECHNICAL_OFFICER','UNDERGRADUATE') NOT NULL,
    first_name           VARCHAR(50)  NOT NULL,
    last_name            VARCHAR(50)  NOT NULL,
    email                VARCHAR(100) NOT NULL UNIQUE,
    nic                  VARCHAR(15)  UNIQUE,
    dob                  DATE,
    phone                VARCHAR(15),
    street               VARCHAR(100),
    city                 VARCHAR(50),
    postal_code          VARCHAR(10),
    country              VARCHAR(50)  DEFAULT 'Sri Lanka',
    profile_picture_path VARCHAR(255),                   -- FR-07 (added: not in EER)
    is_active            BOOLEAN      NOT NULL DEFAULT TRUE, -- FR-05 deactivate (added: not in EER)
    created_date         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- UNDERGRADUATE  (subtype of USER; BELONGS_TO department)
-- stu_id is the same value as users.user_id (shared primary key).
-- ---------------------------------------------------------------------
CREATE TABLE undergraduate (
    stu_id      INT PRIMARY KEY,
    reg_number  VARCHAR(20) NOT NULL UNIQUE,             -- e.g. TG/2024/2095 (added: not in EER)
    intake_date DATE        NOT NULL,
    status      ENUM('REGULAR','REPEAT','BATCH_MISSED') NOT NULL DEFAULT 'REGULAR',
    batch       VARCHAR(10) NOT NULL,
    dep_id      INT         NOT NULL,
    CONSTRAINT fk_ug_user FOREIGN KEY (stu_id) REFERENCES users(user_id)      ON DELETE CASCADE,
    CONSTRAINT fk_ug_dep  FOREIGN KEY (dep_id) REFERENCES department(dep_id) ON DELETE RESTRICT
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- STAFF  (subtype of USER; WORKS_IN department)
-- ADMIN, LECTURER and TECHNICAL_OFFICER are subtypes of STAFF.
-- ---------------------------------------------------------------------
CREATE TABLE staff (
    staff_id INT PRIMARY KEY,
    dep_id   INT NOT NULL,
    CONSTRAINT fk_staff_user FOREIGN KEY (staff_id) REFERENCES users(user_id)      ON DELETE CASCADE,
    CONSTRAINT fk_staff_dep  FOREIGN KEY (dep_id)   REFERENCES department(dep_id)  ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE admin (
    admin_id INT PRIMARY KEY,
    CONSTRAINT fk_admin_staff FOREIGN KEY (admin_id) REFERENCES staff(staff_id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE lecturer (
    lecture_id  INT PRIMARY KEY,
    designation VARCHAR(50),
    CONSTRAINT fk_lect_staff FOREIGN KEY (lecture_id) REFERENCES staff(staff_id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE technical_officer (
    to_id INT PRIMARY KEY,
    CONSTRAINT fk_to_staff FOREIGN KEY (to_id) REFERENCES staff(staff_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- COURSE  (TEACHES: lecturer 1-N course, CREATES: admin 1-N course)
-- ---------------------------------------------------------------------
CREATE TABLE course (
    course_id         INT AUTO_INCREMENT PRIMARY KEY,
    course_code       VARCHAR(10)  NOT NULL UNIQUE,      -- e.g. ICT2132
    course_name       VARCHAR(150) NOT NULL,
    credits_theory    TINYINT      NOT NULL DEFAULT 0,
    credits_practical TINYINT      NOT NULL DEFAULT 0,
    semester          VARCHAR(10)  NOT NULL,             -- e.g. L2S1
    course_type       ENUM('THEORY','PRACTICAL','BOTH') NOT NULL,
    lecture_id        INT NULL,                          -- TEACHES
    created_by        INT NULL,                          -- CREATES (admin)
    CONSTRAINT chk_course_credits CHECK (credits_theory >= 0 AND credits_practical >= 0
                                         AND credits_theory + credits_practical > 0),
    CONSTRAINT fk_course_lect  FOREIGN KEY (lecture_id) REFERENCES lecturer(lecture_id) ON DELETE SET NULL,
    CONSTRAINT fk_course_admin FOREIGN KEY (created_by) REFERENCES admin(admin_id)      ON DELETE SET NULL
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- COURSE_MATERIAL  (UPLOADS: lecturer 1-N, HAS: course 1-N)
-- ---------------------------------------------------------------------
CREATE TABLE course_material (
    material_id INT AUTO_INCREMENT PRIMARY KEY,
    title       VARCHAR(150) NOT NULL,
    file_path   VARCHAR(255) NOT NULL,                   -- (added: not in EER)
    upload_date DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    course_id   INT NOT NULL,
    lecture_id  INT NULL,
    CONSTRAINT fk_cm_course FOREIGN KEY (course_id)  REFERENCES course(course_id)     ON DELETE CASCADE,
    CONSTRAINT fk_cm_lect   FOREIGN KEY (lecture_id) REFERENCES lecturer(lecture_id)  ON DELETE SET NULL
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- ENROLLMENT  (ENROLLS_IN: undergraduate M-N course, with Academic_Year)
-- Academic_Year lets a repeat student enrol in the same course again.
-- ---------------------------------------------------------------------
CREATE TABLE enrollment (
    enrollment_id INT AUTO_INCREMENT PRIMARY KEY,
    stu_id        INT NOT NULL,
    course_id     INT NOT NULL,
    academic_year VARCHAR(9) NOT NULL,                   -- e.g. 2025/2026
    CONSTRAINT uq_enrollment UNIQUE (stu_id, course_id, academic_year),
    CONSTRAINT fk_enr_stu    FOREIGN KEY (stu_id)    REFERENCES undergraduate(stu_id) ON DELETE CASCADE,
    CONSTRAINT fk_enr_course FOREIGN KEY (course_id) REFERENCES course(course_id)     ON DELETE CASCADE
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- MEDICAL_RECORD  (SUBMITS: undergraduate 1-N, APPROVES: technical officer)
-- ---------------------------------------------------------------------
CREATE TABLE medical_record (
    medical_id      INT AUTO_INCREMENT PRIMARY KEY,
    stu_id          INT NOT NULL,
    document_path   VARCHAR(255),
    session_date    DATE NOT NULL,                       -- date the medical covers
    reason          VARCHAR(255),                        -- EER spelling "Resson" corrected
    approval_status ENUM('PENDING','APPROVED','REJECTED') NOT NULL DEFAULT 'PENDING',
    approved_by     INT NULL,                            -- technical officer who decided
    CONSTRAINT fk_med_stu FOREIGN KEY (stu_id)      REFERENCES undergraduate(stu_id)      ON DELETE CASCADE,
    CONSTRAINT fk_med_to  FOREIGN KEY (approved_by) REFERENCES technical_officer(to_id)   ON DELETE SET NULL
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- ATTENDANCE  (weak entity; SUBMIT: technical officer 1-N,
-- HAS: undergraduate 1-N, HAS: course 1-N, HAS: medical 1-N)
-- session_id = session number within the course (1..15).
-- session_hours = 2 per session (1 credit = 2 hours, per project brief).
-- ---------------------------------------------------------------------
CREATE TABLE attendance (
    attendance_id INT AUTO_INCREMENT PRIMARY KEY,
    stu_id        INT NOT NULL,
    course_id     INT NOT NULL,
    to_id         INT NULL,                              -- who recorded it
    medical_id    INT NULL,                              -- medical covering this absence
    session_id    TINYINT NOT NULL,
    session_type  ENUM('THEORY','PRACTICAL') NOT NULL,
    session_date  DATE NOT NULL,
    status        ENUM('PRESENT','ABSENT') NOT NULL,
    session_hours DECIMAL(3,1) NOT NULL DEFAULT 2.0,
    CONSTRAINT chk_att_session CHECK (session_id BETWEEN 1 AND 15),
    CONSTRAINT uq_attendance UNIQUE (stu_id, course_id, session_type, session_date),
    CONSTRAINT fk_att_stu    FOREIGN KEY (stu_id)     REFERENCES undergraduate(stu_id)     ON DELETE CASCADE,
    CONSTRAINT fk_att_course FOREIGN KEY (course_id)  REFERENCES course(course_id)         ON DELETE CASCADE,
    CONSTRAINT fk_att_to     FOREIGN KEY (to_id)      REFERENCES technical_officer(to_id)  ON DELETE SET NULL,
    CONSTRAINT fk_att_med    FOREIGN KEY (medical_id) REFERENCES medical_record(medical_id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- MARKS  (weak entity; HAS: undergraduate 1-N, HAS: course 1-N)
-- All marks are out of 100 (project brief).
-- mark_category marks whether a mark counts toward CA or the final exam,
-- needed for the "CA >= 40%" eligibility rule (added: not in EER).
-- ---------------------------------------------------------------------
CREATE TABLE marks (
    mark_id         INT AUTO_INCREMENT PRIMARY KEY,
    stu_id          INT NOT NULL,
    course_id       INT NOT NULL,
    evaluation_type VARCHAR(30) NOT NULL,                -- e.g. QUIZ, ASSIGNMENT, MID, FINAL
    mark_category   ENUM('CA','FINAL') NOT NULL DEFAULT 'CA',
    marks_value     DECIMAL(5,2) NOT NULL,
    CONSTRAINT chk_marks_range CHECK (marks_value BETWEEN 0 AND 100),
    CONSTRAINT fk_marks_stu    FOREIGN KEY (stu_id)    REFERENCES undergraduate(stu_id) ON DELETE CASCADE,
    CONSTRAINT fk_marks_course FOREIGN KEY (course_id) REFERENCES course(course_id)     ON DELETE CASCADE
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- GRADE  (computed final grade per student per course - UGC circular 12-2024)
-- SGPA / CGPA are calculated from this table by the service layer.
-- ---------------------------------------------------------------------
CREATE TABLE grade (
    grade_id    INT AUTO_INCREMENT PRIMARY KEY,
    stu_id      INT NOT NULL,
    course_id   INT NOT NULL,
    final_mark  DECIMAL(5,2) NOT NULL,
    grade       VARCHAR(3)   NOT NULL,                   -- A+, A, A-, B+, ... E
    grade_point DECIMAL(3,2) NOT NULL,
    semester    VARCHAR(10)  NOT NULL,
    CONSTRAINT chk_grade_mark  CHECK (final_mark BETWEEN 0 AND 100),
    CONSTRAINT chk_grade_point CHECK (grade_point BETWEEN 0 AND 4),
    CONSTRAINT fk_grade_stu    FOREIGN KEY (stu_id)    REFERENCES undergraduate(stu_id) ON DELETE CASCADE,
    CONSTRAINT fk_grade_course FOREIGN KEY (course_id) REFERENCES course(course_id)     ON DELETE CASCADE
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- NOTICE  (POSTS: admin 1-N, HAVE: department 1-N)
-- dep_id NULL = faculty-wide notice.
-- ---------------------------------------------------------------------
CREATE TABLE notice (
    notice_id   INT AUTO_INCREMENT PRIMARY KEY,
    title       VARCHAR(150) NOT NULL,
    content     TEXT NOT NULL,
    posted_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    target_role ENUM('ALL','ADMIN','LECTURER','TECHNICAL_OFFICER','UNDERGRADUATE') NOT NULL DEFAULT 'ALL',
    posted_by   INT NULL,
    dep_id      INT NULL,
    CONSTRAINT fk_notice_admin FOREIGN KEY (posted_by) REFERENCES admin(admin_id)      ON DELETE SET NULL,
    CONSTRAINT fk_notice_dep   FOREIGN KEY (dep_id)    REFERENCES department(dep_id)   ON DELETE SET NULL
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- TIMETABLE  (CREATES: admin 1-N, SCHEDULED: course 1-N, HAVE: department 1-N)
-- ---------------------------------------------------------------------
CREATE TABLE timetable (
    timetable_id INT AUTO_INCREMENT PRIMARY KEY,
    course_id    INT NOT NULL,
    dep_id       INT NOT NULL,
    created_by   INT NULL,
    day          ENUM('MON','TUE','WED','THU','FRI','SAT','SUN') NOT NULL,
    start_time   TIME NOT NULL,
    end_time     TIME NOT NULL,
    session_type ENUM('THEORY','PRACTICAL') NOT NULL,
    venue        VARCHAR(50),
    CONSTRAINT chk_tt_time CHECK (end_time > start_time),
    CONSTRAINT fk_tt_course FOREIGN KEY (course_id)  REFERENCES course(course_id)     ON DELETE CASCADE,
    CONSTRAINT fk_tt_dep    FOREIGN KEY (dep_id)     REFERENCES department(dep_id)    ON DELETE RESTRICT,
    CONSTRAINT fk_tt_admin  FOREIGN KEY (created_by) REFERENCES admin(admin_id)       ON DELETE SET NULL
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Helpful indexes for the batch / individual summary queries
-- ---------------------------------------------------------------------
CREATE INDEX idx_att_stu_course   ON attendance (stu_id, course_id);
CREATE INDEX idx_marks_stu_course ON marks (stu_id, course_id);
CREATE INDEX idx_grade_stu_sem    ON grade (stu_id, semester);
CREATE INDEX idx_ug_batch         ON undergraduate (batch);

-- ---------------------------------------------------------------------
-- OPTIONAL (SEC-05): dedicated application user instead of root.
-- Run this by hand on your own machine with your own password.
-- Do NOT commit a real password to Git (SEC-08).
-- ---------------------------------------------------------------------
-- CREATE USER 'faculty_app'@'localhost' IDENTIFIED BY '<choose-a-password>';
-- GRANT SELECT, INSERT, UPDATE, DELETE ON faculty_db.* TO 'faculty_app'@'localhost';
-- FLUSH PRIVILEGES;
