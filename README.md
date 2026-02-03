## MedHead_PoC_systeme_d_intervention_d_urgence

MedHead Dispatch is a specialized logistics and decision-support application designed to optimize the redirection of patients to medical facilities based on real-time data. Its primary goal is to ensure that patients are sent to the most appropriate hospital as quickly as possible, taking into account both medical needs and current hospital capacity.


## Features

##### Core Objectives
* Emergency Classification: The system allows medical dispatchers or professionals to quickly identify the nature of an emergency (e.g., Cardiology, Orthopedics) through an intuitive, searchable interface.

* Real-Time Bed Management: By connecting to a centralized API, the application retrieves the live availability of beds in various specialized care units.

* Geospatial Optimization: The platform calculates the most efficient route between a patient's current location (via GPS or manual address entry) and available hospitals, providing estimated travel times and distances.

* Instant Resource Reservation: To prevent hospitals from becoming overwhelmed and to guarantee patient care upon arrival, the system enables the immediate reservation of a bed in a specific unit with a single click.

## Demo Video
[![Démonstration MedHead](https://img.youtube.com/vi/7hYXP1wDDu4/maxresdefault.jpg)](https://www.youtube.com/watch?v=7hYXP1wDDu4)

## Stack

#### Data
![MariaDB](https://img.shields.io/badge/MariaDB-003545?style=for-the-badge&logo=mariadb&logoColor=white)

#### Back
 ![Maven](https://img.shields.io/badge/Apache_Maven-C71A36?style=for-the-badge&logo=apache-maven&logoColor=white) ![Spring Boot](https://img.shields.io/badge/Spring_Boot-F2F4F9?style=for-the-badge&logo=spring-boot&logoColor=6DB33F) ![Spring Security](https://img.shields.io/badge/Spring%20Security-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white)


#### Front
![Bootstrap](https://img.shields.io/badge/bootstrap-%238511FA.svg?style=for-the-badge&logo=bootstrap&logoColor=white) ![Vue.js](https://img.shields.io/badge/vue.js-%2335495e.svg?style=for-the-badge&logo=vuedotjs&logoColor=%234FC08D)

#### CI/CD
![Git](https://img.shields.io/badge/git-%23F05033.svg?style=for-the-badge&logo=git&logoColor=white) ![GitHub](https://img.shields.io/badge/github-%23121011.svg?style=for-the-badge&logo=github&logoColor=white) ![Docker](https://img.shields.io/badge/docker-%230db7ed.svg?style=for-the-badge&logo=docker&logoColor=white) ![Jenkins](https://img.shields.io/badge/jenkins-%232C5263.svg?style=for-the-badge&logo=jenkins&logoColor=white)  ![SonarQube](https://img.shields.io/badge/SonarQube-4E9BCD?style=for-the-badge&logo=sonarqube&logoColor=white) ![JUnit5](https://img.shields.io/badge/JUnit5-25A162?style=for-the-badge&logo=junit5&logoColor=white) ![Newman](https://img.shields.io/badge/Newman-FF6C37?style=for-the-badge&logo=postman&logoColor=white&labelColor=orange)  ![Puppeteer](https://img.shields.io/badge/Puppeteer-%2300d8a2.svg?style=for-the-badge&logo=puppeteer&logoColor=white)


## Prerequisites

* **Git**: To clone the repository. [Download here](https://git-scm.com/downloads)
*  **Docker & Docker Compose**: To run the application and the database in containers. [Download here](https://docs.docker.com/get-docker/)

##  Sart the app

1°/ Open the command prompt where you want to place the project.
2°/ Clone the prject

```sh
git clone https://github.com/EmrysC/MedHead_PoC_systeme_d_intervention_d_urgence 
```
3°/ Move on the docker's folder

```sh
cd "MedHead_PoC_systeme_d_intervention_d_urgence/Poc/Poc/src/main/resources" 
```

4°/ Put the .env in the same folder

![Configuration Environnement](https://raw.githubusercontent.com/EmrysC/MedHead_PoC_systeme_d_intervention_d_urgence/main/documentation/Readme_doc/env.png)

[Click here to see the .env example file](https://github.com/EmrysC/MedHead_PoC_systeme_d_intervention_d_urgence/blob/main/documentation/Readme_doc/exemple.env)

5°/Run the app
```sh
docker-compose up -d app 
```
![Run the app](https://raw.githubusercontent.com/EmrysC/MedHead_PoC_systeme_d_intervention_d_urgence/main/documentation/Readme_doc/docker_app.png)

6°/ Populating the Database

The database schema is intentionally initialized without data at startup. Therefore, a dataset must be imported manually. We will use the same dataset utilized by our CI/CD pipeline.

```sh
Get-Content "sql-init/import.sql" | docker exec -i resources-db mariadb -u root -pexample poc_db
```

> You can launch the database management interface by running: `docker-compose up -d adminer` 

7°/ Acces to the app with Services & Access

## Services & Access


| Docker Command (Run) | Access Link | Description | Connection |
| :--- | :--- | :--- | :--- |
| `docker-compose up -d app` | [http://localhost:8080](http://localhost:8080/api/login ) | **Main Application**: Access the MedHead emergency system interface (also sart the db docker). |  utilisateur1@compte.com / MotDePasseSecret&1
|  | [http://localhost:8080/api/swagger-ui/index.html](http://localhost:8080/api/swagger-ui/index.html) | **Swagger UI**: End point documantation. |
| `docker-compose up -d adminer` | [http://localhost:8081](http://localhost:8081) | **MariaDB Database**: Persistent storage for hospitals and emergency data(the data base need to be sarted). |   root / example
| `docker-compose up -d jenkins` | [http://localhost:8082](http://localhost:8082) | **Jenkins Server**: Automation server for CI/CD pipelines (you need some setup credentials) . |
| `docker-compose up -d newman-dashboard` | [http://localhost:8083](http://localhost:8083) | **Newman Reports**: ewman Reports: Dashboard for viewing Postman API test results. |
| `docker-compose up -d sonarqube` | [http://localhost:8084](http://localhost:8084) | **SonarQube Dashboard**: Code quality and security analysis platform. |  admin / admin

## Set up CI/CD pipeline

[Click here to see the CICD configuration](https://github.com/EmrysC/MedHead_PoC_systeme_d_intervention_d_urgence/blob/main/documentation/cicd/CICD_conf.pdf)
