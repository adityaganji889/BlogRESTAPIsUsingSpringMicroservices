![image](https://github.com/user-attachments/assets/89138d97-084e-4ca9-9757-2112bec0570f)# Blogging Platform REST APIs Using Spring Microservices
  RESTful API for Users that allows them to register/login in this and then to post, read, edit, and delete their blogs.

## Features:
 * RESTful API with endpoints for creating, reading, updating, and deleting blogs. 
 * Each user detail have atleast an email(username), password, role.
 * User can register as either of the 2 roles available: USER, ADMIN.
 * Made use of Eureka Discovery Service for registering microservices: API-GATEWAY, CONFIG-SERVICE, USER-SERVICE, BLOG-SERVICE.
 * Fetching application.properties for CONFIG-SERVICE, USER-SERVICE, BLOG-SERVICE from SQL DB, properties table.
 * USER-SERVICE stores users info in SQL DB, users table.
 * BLOG-SERVICE stores blogs info in NoSQL DB, blogs collection.
 * Each microservice running on different ports, other than discovery service all others microservices could be visible in eureka service registry when they're up and running.
 * Provided clear and comprehensive API documentation using tools like Swagger or OpenAPI.

## Tech Stack Used:

#### Back-End:
<img alt="Spring-Boot" src="https://img.shields.io/badge/Spring-6DB33F?style=for-the-badge&logo=Spring-Boot&logoColor=white"/> <img alt="Hibernate" src="https://img.shields.io/badge/Hibernate-59666C?style=for-the-badge&logo=Hibernate&logoColor=white"/> <img alt="Swagger UI" src ="https://img.shields.io/badge/-Swagger-%23Clojure?style=for-the-badge&logo=swagger&logoColor=white"/> <img alt="JWT" src ="https://img.shields.io/badge/JWT-red?style=for-the-badge&logo=JSON+Web+Tokens&logoColor=white"/> 
#### Database:
<img alt="MySQL" src="https://img.shields.io/badge/mysql-%2300f.svg?style=for-the-badge&logo=mysql&logoColor=white"/><img alt="MongoDB" src ="https://img.shields.io/badge/MongoDB-4EA94B?style=for-the-badge&logo=mongodb&logoColor=white"/>

## Demonstration:
![Eureka1](https://github.com/user-attachments/assets/73740a55-297f-4334-84b5-ae7f68707563)
![Eureka2](https://github.com/user-attachments/assets/03508662-658a-4fc4-8c4b-3528539b34d7)
![Config](https://github.com/user-attachments/assets/79e70899-4e8a-40e1-bf75-cd141a2babd2)
![Config_1](https://github.com/user-attachments/assets/f85d0ea1-7b93-43a5-8871-19894e27e7d3)
![User](https://github.com/user-attachments/assets/6f770f69-5607-4b5b-8160-01047218fa22)
![Blog](https://github.com/user-attachments/assets/5a844349-86d5-40ac-b8f5-d5bef10a276b)
![Users](https://github.com/user-attachments/assets/948419d8-7295-45d0-bee1-09c06d0ea889)
![Blogs](https://github.com/user-attachments/assets/4f189187-efd6-422a-b169-d630cee94018)
![Auth1](https://github.com/user-attachments/assets/ba44e285-94e1-4465-be67-2d2620f574ed)
![Auth2](https://github.com/user-attachments/assets/fd1968f8-734a-4620-a48c-73662339a02b)












