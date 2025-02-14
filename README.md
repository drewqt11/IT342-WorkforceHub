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
        <li><a href="#feature-7">Feature 7</a></li>
        <li><a href="#feature-8">Feature 8</a></li>
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

- **Backend**: Django (Python) - A high-level web framework for efficient and scalable backend development.  
- **Database**: PostgreSQL - A powerful and reliable database system for managing structured data.  
- **Frontend**: Tailwind CSS - A utility-first CSS framework for designing responsive and customizable user interfaces.  


## List Of Features
- **Employee Profiles**: Centralized database for managing employee records and personal information.
- **Attendance Management**: Efficiently track attendance, leaves, and absences with real-time counts for absentees, leaves, and those present, while providing comprehensive records, with overtime and  regular hours record to ensure transparency and accountability.
- **Payroll Management**: Automated calculations, deductions, and payments with secure storage and accessibility
- **Performance Management**: Tools for peer feedback, self-assessment, supervisor feedback, and sanction reporting
- **Recruitment Tools**: Simplifies the hiring process with applicant tracking, resume management, and scheduling capabilities.
- **Analytics Dashboard**: Real-time insights into HR data to support decision-making and compliance.

## Getting Started

### Prerequisites
Ensure you have the following:
- Python 3.x
- Django installed in a virtual environment
- PostgreSQL with `pgAdmin`
- Basic understanding of Django ORM and SQL

### Installation

**Tailwind CSS Framework Setup** 


1. **Modify `INSTALLED_APPS` in `settings.py`**
   - Add `'tailwind'` to the list of installed apps.

2. **Install dependencies and initialize Tailwind**
   - In Powershell (inside the virtual environment `myenv`), run:
     ```bash
     python -m pip install django-tailwind
     ```
   - Navigate to your project directory:
     ```bash
     cd projectname
     ```
   - Initialize Tailwind:
     ```bash
     python manage.py tailwind init
     ```
   - When prompted for `app_name[theme]`, type `theme`.
   - Add `'theme'` and `'django_browser_reload'` to `INSTALLED_APPS` in `settings.py`.
   - Install the browser reload package:
     ```bash
     pip install django-browser-reload
     ```

3. **Update `settings.py`**
   - Add the following lines:
     ```python
     TAILWIND_APP_NAME = 'theme'
     INTERNAL_IPS = ["127.0.0.1",]
     ```

4. **Add middleware**
   - Append the following line to the `MIDDLEWARE` list in `settings.py`:
     ```python
     "django_browser_reload.middleware.BrowserReloadMiddleware",
     ```

5. **Update `urls.py`**
   - Add the following line to the project's `urls.py` file:
     ```python
     path("reload/", include("django_browser_reload.urls")),
     ```

6. **Start Tailwind**
   - Run:
     ```bash
     python manage.py tailwind start


**PostgreSQL Installation** 


1. **Download PostgreSQL Installer**
   - Download the PostgreSQL `.exe` from the following link:
     [PostgreSQL Download](https://www.postgresql.org/ftp/pgadmin/pgadmin4/v8.12/windows/)

2. **Install Required Components**
   - During installation, ensure that `pgAdmin` is selected as a component.

3. **Create the Database**
   - Open `pgAdmin`.
   - Create a new database:
     - Right-click on `Databases` > `Create` > `Database`.
     - Name the database `dbhrmanagement`.
     - In the **General Tab**, set the **Owner** to `postgres`.
   - Check the database files in `pgAdmin` for additional settings or configurations.
   - Use the **Definition Tab** for further configuration if necessary.

4. **Execute Commands in pgAdmin**
   - To run SQL commands, right-click on the database and select `Query Tool`.


## Additional Resources

- [Functional Requirements Document](https://docs.google.com/document/d/1wRUX7TfamZ61ei4otYVEJMC07NCnUOh7NaZMVRlH61s/edit?tab=t.0)
- 



## Developers Profile

  
- [Katrina Amores](https://github.com/katkatty21): katrina.amores@cit.edu







