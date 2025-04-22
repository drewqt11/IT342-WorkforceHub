<!-- PROJECT LOGO -->
<div align="center">
    <img src="https://github.com/drewqt11/IT342-WorkforceHub/blob/bd1cc260de64edbaacd0f03b2e5184805cd76570/Logo%20with%20Background.png" alt="logoText">
    <h3>HR Information System READ ME</h3>
</div>

<details>
  <summary>Table of Contents</summary>
  <ol>
    <li>
      <h5>About The Project</h5>
      <ul>
        <li><a href="#product-description">Product Description</a></li>
          <li><a href="#built-with">Built With</a></li>
      </ul>
    </li>
       <li>
       <h5>List of Features</h5>
      <ul>
        <li><a href="#feature-1">Feature 1</a></li>
        <li><a href="#feature-2">Feature 2</a></li>
        <li><a href="#feature-3">Feature 3</a></li>
        <li><a href="#feature-4">Feature 4</a></li>
        <li><a href="#feature-5">Feature 5</a></li>
        <li><a href="#feature-6">Feature 6</a></li>
        
      </ul>
    </li>
    <li>
       <h5>Getting Started</h5>
      <ul>
        <li><a href="#prerequisites">Prerequisites</a></li>
        <li><a href="#installation">Installation</a></li>
      </ul>
    </li>
     <li><a href="#additional-resources">Additional Resources</a></li>
     <li><a href="#contact">Developers Profiles</a></li>
  </ol>
</details>

## Product Description

The **HR Management System** is a comprehensive platform designed to streamline and optimize the core functions of Human Resources. Built to simplify processes like employee data management, attendance tracking, payroll processing, performance evaluations, and recruitment, this system serves as an all-in-one solution for HR departments of any size. By centralizing data and automating repetitive tasks, it reduces administrative workload and helps HR teams focus on strategic activities, such as talent development and employee engagement.

## Built With

- **Backend**: Spring Boot (Java) - A framework for building stand-alone, production-grade Spring-based applications.
- **Frontend**: Next.js and React - JavaScript frameworks for building user interfaces, with server-side rendering for better performance.
- **UI Design**: ShadCN UI - A UI component library for building modern and elegant user interfaces.
- **Database**: SupaBase - An open-source Firebase alternative, offering a database, authentication, and real-time subscriptions out of the box.
- **CSS Framework**: Tailwind CSS - A utility-first CSS framework for designing responsive and customizable user interfaces.

## List Of Features

- **Employee Profiles**: Centralized database for managing employee records and personal information.
- **Attendance Management**: Efficiently track attendance, leaves, and absences with real-time counts for absentees, leaves, and those present, while providing comprehensive records, with overtime and regular hours record to ensure transparency and accountability.
- **Payroll Management**: Automated calculations, deductions, and payments with secure storage and accessibility.
- **Performance Management**: Tools for peer feedback, self-assessment, supervisor feedback, and sanction reporting.
- **Recruitment Tools**: Simplifies the hiring process with applicant tracking, resume management, and scheduling capabilities.
- **Analytics Dashboard**: Real-time insights into HR data to support decision-making and compliance.

## Getting Started

### Prerequisites

Ensure you have the following:

- Java 8 or later installed
- Maven or Gradle for dependency management
- Node.js and npm/yarn for frontend development
- SupaBase account for managing database and authentication

### Installation

**Tailwind CSS Framework Setup**

1. **Install Tailwind CSS for Next.js**
   - In the root directory of your Next.js project, run:
     ```bash
     npm install -D tailwindcss postcss autoprefixer
     npx tailwindcss init
     ```
   - In `tailwind.config.js`, configure the content array:
     ```js
     module.exports = {
       content: [
         "./pages/**/*.{js,ts,jsx,tsx}",
         "./components/**/*.{js,ts,jsx,tsx}",
       ],
       theme: {
         extend: {},
       },
       plugins: [],
     };
     ```
   - Add the following lines to `globals.css`:
     ```css
     @tailwind base;
     @tailwind components;
     @tailwind utilities;
     ```

**Spring Boot Setup**

1. **Install Spring Boot**

   - Download and install the Spring Boot CLI or set up Spring Boot with Maven or Gradle.
   - Add the necessary dependencies for the backend application in `pom.xml` (Maven) or `build.gradle` (Gradle).

2. **Configure Database (SupaBase)**

   - Create an account on [SupaBase](https://supabase.io).
   - Create a new project and configure the database.
   - Use SupaBase’s SDK or API to connect your Spring Boot backend to SupaBase for database operations and authentication.

3. **Run the Backend**

   - Run the Spring Boot application using Maven or Gradle:
     ```bash
     ./mvnw spring-boot:run  # For Maven
     ./gradlew bootRun       # For Gradle
     ```

4. **Run the Frontend**
   - Navigate to the Next.js frontend project directory:
     ```bash
     cd frontend
     npm run dev
     ```

## Additional Resources

- [Functional Requirements Document](https://docs.google.com/document/d/1wRUX7TfamZ61ei4otYVEJMC07NCnUOh7NaZMVRlH61s/edit?tab=t.0)
- [Updated Functional Requirements Document](https://cebuinstituteoftechnology-my.sharepoint.com/:w:/g/personal/katrina_amores_cit_edu/EcKCZX2rRKtHpAiAEIdhUQ0BC6tVMw66-Pq-vTHT5WyFwQ?e=hWRVH3)
- [Software Requirements Specifications](https://cebuinstituteoftechnology-my.sharepoint.com/:w:/g/personal/katrina_amores_cit_edu/EbhEGIjYnZZLuXWp3GFqGHsBVuWBVY9O9h7UmPx5BTm1RQ?e=IP44E5)
- [Workforce Hub Structuring](???)

## Developers Profile

- [Katrina Amores](https://github.com/katkatty21): katrina.amores@cit.edu
- [Andri Apas](https://github.com/drewqt11): andri.apas@cit.edu
- [Arnel Paden](https://github.com/padsssss): arnel.paden@cit.edu
- [Levi Caaway](https://github.com/LiarsLiedLies): leviray.caaway@cit.edu
