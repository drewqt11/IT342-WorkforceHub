-- Insert admin user (password is 'password123' bcrypted)
INSERT INTO users (username, email, password, two_factor_enabled)
VALUES ('admin', 'admin@example.com', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', false)
ON CONFLICT (username) DO NOTHING;

-- Insert admin roles
INSERT INTO user_roles (user_id, role)
VALUES (1, 'ADMIN'),
       (1, 'HR_STAFF')
ON CONFLICT DO NOTHING;

-- Insert employee user
INSERT INTO users (username, email, password, two_factor_enabled)
VALUES ('employee', 'employee@example.com', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', false)
ON CONFLICT (username) DO NOTHING;

-- Insert employee role
INSERT INTO user_roles (user_id, role)
VALUES (2, 'EMPLOYEE')
ON CONFLICT DO NOTHING;

-- Insert employee profile
INSERT INTO employees (user_id, first_name, last_name, email, phone_number, date_of_birth, hire_date, department, position)
VALUES (2, 'John', 'Doe', 'john.doe@example.com', '123-456-7890', '1990-01-01', '2023-01-15', 'IT', 'Software Developer')
ON CONFLICT (email) DO NOTHING;

-- Insert test user with ID 5
INSERT INTO users (id, username, email, password, two_factor_enabled)
VALUES (5, 'testuser', 'testuser@example.com', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', false)
ON CONFLICT (id) DO NOTHING;

-- Insert test user roles
INSERT INTO user_roles (user_id, role)
VALUES (5, 'ADMIN'),
       (5, 'HR_STAFF')
ON CONFLICT DO NOTHING; 