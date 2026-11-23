-- Import this in XAMPP: http://localhost/phpmyadmin -> Import (or paste in SQL tab)
CREATE DATABASE IF NOT EXISTS ustp_task_tracker CHARACTER SET utf8mb4;
USE ustp_task_tracker;

CREATE TABLE IF NOT EXISTS users (
  id INT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(60) UNIQUE NOT NULL,
  password_hash CHAR(64) NOT NULL,
  full_name VARCHAR(120), email VARCHAR(120), student_id VARCHAR(30),
  college VARCHAR(150), major VARCHAR(150), semester VARCHAR(120),
  avatar_path VARCHAR(300)
);

CREATE TABLE IF NOT EXISTS tasks (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  name VARCHAR(150) NOT NULL,
  subject VARCHAR(30) NOT NULL,
  description TEXT,
  deadline DATE NOT NULL,
  status ENUM('Pending','Completed') NOT NULL DEFAULT 'Pending',
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Remembers which deadline emails were already sent (so nobody gets the same email twice).
-- The app also creates this table automatically, so existing databases do not need to be re-imported.
CREATE TABLE IF NOT EXISTS reminder_log (
  id INT AUTO_INCREMENT PRIMARY KEY,
  task_id INT NOT NULL,
  stage VARCHAR(8) NOT NULL,
  sent_on DATE NOT NULL,
  UNIQUE KEY uq_reminder (task_id, stage, sent_on),
  FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE
);

-- Default account: username = student   password = 12345678
INSERT INTO users (username,password_hash,full_name,email,student_id,college,major,semester)
VALUES ('student', SHA2('12345678',256), 'Kate S. Pangasi-an', 'katepangasian4@gmail.com', '2025300288',
 'College of Information Technology and Computing', 'Bachelor of Science in Information Technology (BSIT)',
 '1st Semester Academic Year 2026-2027');

INSERT INTO tasks (user_id,name,subject,description,deadline,status) VALUES
 (1,'Activity 3','IT215','Activity 3 requirements',DATE_ADD(CURDATE(),INTERVAL 1 DAY),'Completed'),
 (1,'Proposal Defense','IT214','Proposal Defense - Flowchart and High-Level Prototype',DATE_ADD(CURDATE(),INTERVAL 11 DAY),'Pending'),
 (1,'ERD Proposal Defense','IT212','ERD for the proposal',DATE_ADD(CURDATE(),INTERVAL 90 DAY),'Pending');
