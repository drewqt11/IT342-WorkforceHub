-- Insert admin user (password is 'password123' bcrypted)
INSERT INTO users (first_name, last_name, username, email, password)
VALUES ('Admin', 'User', 'admin', 'admin@example.com', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG'); 