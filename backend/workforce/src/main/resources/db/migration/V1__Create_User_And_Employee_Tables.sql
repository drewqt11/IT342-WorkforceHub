-- Create user_account table
CREATE TABLE IF NOT EXISTS user_account (
    user_id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    email_address VARCHAR(255) UNIQUE NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE
);

-- Create employee table
CREATE TABLE IF NOT EXISTS employee (
    employee_id UUID PRIMARY KEY,
    user_id UUID,
    id_number VARCHAR(50),
    first_name VARCHAR(150),
    middle_name VARCHAR(150),
    last_name VARCHAR(150),
    email VARCHAR(255),
    gender VARCHAR(50),
    contact_number VARCHAR(50),
    address TEXT,
    date_of_birth DATE,
    hire_date DATE,
    civil_status VARCHAR(50),
    nationality VARCHAR(100),
    profile_photo_url VARCHAR(255),
    employment_urls TEXT,
    employment_status VARCHAR(50) DEFAULT 'ACTIVE' NOT NULL
);
