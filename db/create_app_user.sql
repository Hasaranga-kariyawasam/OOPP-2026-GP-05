-- =====================================================================
-- Faculty Management System - application database user (SEC-05)
-- Run this ONCE on your own machine, AFTER db/schema.sql, as root:
--
--     mysql -u root -p < db/create_app_user.sql
--     (XAMPP: root has no password, so just:  mysql -u root < db/create_app_user.sql)
--
-- BEFORE RUNNING: replace CHANGE_ME below with your own password.
-- Do NOT commit your real password to Git (SEC-08). Put the same password
-- in your local db.properties file, which is git-ignored.
-- =====================================================================

DROP USER IF EXISTS 'faculty_app'@'localhost';

CREATE USER 'faculty_app'@'localhost' IDENTIFIED BY 'CHANGE_ME';

-- Only the privileges the app needs: no DROP / ALTER / CREATE.
GRANT SELECT, INSERT, UPDATE, DELETE ON faculty_db.* TO 'faculty_app'@'localhost';

FLUSH PRIVILEGES;

-- Check it worked:
SHOW GRANTS FOR 'faculty_app'@'localhost';
