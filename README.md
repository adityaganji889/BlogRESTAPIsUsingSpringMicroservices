# Blogging Platform REST APIs Using Spring Microservices
  RESTful API for Users that allows them to register/login in this and then to post, read, edit, and delete their blogs.

## Features:
 * RESTful API with endpoints for creating, reading, updating, and deleting blogs. 
 * Each user detail have atleast an email(username), password, role.
 * User can register as either of the 2 roles available: USER, ADMIN.
 * Admin User can make change other user roles to Admin/User, can delete a user including cascade delete of his/her blogs, can get all users info.
 * User can get blog to read by blog id, can get all blogs to read, can post new blogs, can update, delete their created blogs.
 * Made use of Netflix Eureka Discovery Service for registering microservices: API-GATEWAY, CONFIG-SERVICE, USER-SERVICE, BLOG-SERVICE.
 * Fetching application.properties for CONFIG-SERVICE, USER-SERVICE, BLOG-SERVICE from SQL DB, properties table.
 * USER-SERVICE stores users info in SQL DB, users table.
 * BLOG-SERVICE stores blogs info in NoSQL DB, blogs collection.
 * Each microservice running on different ports, other than discovery service all others microservices could be visible in eureka service registry when they're up and running.
 * Each microservice is packaged as a Docker container and deployed on Render Cloud.
 * Provided clear and comprehensive API documentation using tools like Swagger or OpenAPI.

## High Level Design (HLD):
```mermaid
flowchart TB

    Client["👤 Client<br/>Browser / Postman"]

    Gateway["API Gateway<br/>Spring Cloud Gateway"]

    UserService["User Service<br/>Spring Boot"]

    BlogService["Blog Service<br/>Spring Boot"]

    Eureka["Eureka Discovery Server"]

    Config["Config Server"]

    MySQL[("MySQL<br/>Aiven Cloud")]

    MongoDB[("MongoDB Atlas")]

    Client -->|"HTTPS + JWT"| Gateway

    Gateway -->|"/api/users/**,/api/userProfile/**,/api/admin/**"| UserService
    Gateway -->|"/api/blogs/**"| BlogService

    BlogService -->|"REST API"| UserService

    UserService --> MySQL
    BlogService --> MongoDB

    Gateway -. Registers .-> Eureka
    UserService -. Registers .-> Eureka
    BlogService -. Registers .-> Eureka
    Config -. Registers .-> Eureka

    Gateway -. Fetch Config .-> Config
    UserService -. Fetch Config .-> Config
    BlogService -. Fetch Config .-> Config
```
## Low Level Design (LLD):

```mermaid
flowchart LR

%% ================= CLIENT ===================
Client["👤 Client<br/>Browser / Postman"]

%% ================= API GATEWAY ===================
subgraph Gateway["API Gateway"]

GatewayController["Spring Cloud Gateway"]

JwtHeaderFilter["JWT Header Filter"]

Routes["Route Predicates"]

GatewayController --> JwtHeaderFilter
JwtHeaderFilter --> Routes

end

%% ================= USER SERVICE ===================
subgraph UserService["USER-SERVICE"]

UserController["Controllers"]

UserServiceLayer["Service Layer"]

UserRepository["JPA Repository"]

JwtFilter["JWT Authentication Filter"]

SpringSecurity["Spring Security"]

UserController --> UserServiceLayer
UserServiceLayer --> UserRepository

SpringSecurity --> JwtFilter
JwtFilter --> UserController

end

%% ================= BLOG SERVICE ===================
subgraph BlogService["BLOG-SERVICE"]

BlogController["Controllers"]

BlogServiceLayer["Service Layer"]

MongoRepository["Mongo Repository"]

RestTemplate["RestTemplate (LoadBalanced)"]

BlogController --> BlogServiceLayer
BlogServiceLayer --> MongoRepository
BlogServiceLayer --> RestTemplate

end

%% ================= CONFIG SERVER ===================
subgraph ConfigServer["CONFIG-SERVICE"]

ConfigController["Spring Cloud Config Server"]

DBPropertySource["Database Property Source"]

ConfigController --> DBPropertySource

end

%% ================= DISCOVERY ===================
subgraph Discovery["DISCOVERY-SERVICE"]

Eureka["Netflix Eureka Server"]

end

%% ================= DATABASES ===================

MySQL[("MySQL Database")]

MongoDB[("MongoDB Atlas")]

UserRepository --> MySQL

DBPropertySource --> MySQL

MongoRepository --> MongoDB

%% ================= REQUEST FLOW ===================

Client --> GatewayController

Routes --> UserController
Routes --> BlogController

RestTemplate --> UserController

%% ================= DISCOVERY ===================

GatewayController -. Registers .-> Eureka
UserController -. Registers .-> Eureka
BlogController -. Registers .-> Eureka
ConfigController -. Registers .-> Eureka

%% ================= CONFIG FETCH ===================

GatewayController -. Fetch Config .-> ConfigController
UserController -. Fetch Config .-> ConfigController
BlogController -. Fetch Config .-> ConfigController

```

## Deployment Diagram:

```mermaid
flowchart LR

subgraph Render["Render Cloud"]

Gateway["API Gateway"]

Eureka["Eureka Server"]

Config["Config Server"]

User["User Service"]

Blog["Blog Service"]

end

MySQL[("Aiven MySQL")]

MongoDB[("MongoDB Atlas")]

User --> MySQL

Blog --> MongoDB

Gateway --> User

Gateway --> Blog

User --> Eureka

Blog --> Eureka

Gateway --> Eureka

Config --> Eureka
```
## Sequence Diagram:

```mermaid
sequenceDiagram
    autonumber

    actor User
    participant Gateway as API Gateway
    participant UserService as USER-SERVICE
    participant BlogService as BLOG-SERVICE
    participant Eureka as DISCOVERY-SERVICE
    participant Config as CONFIG-SERVICE
    participant MySQL
    participant MongoDB

    Note over Gateway,Config: Application Startup

    Gateway->>Config: Fetch Gateway Configuration
    UserService->>Config: Fetch User Configuration
    BlogService->>Config: Fetch Blog Configuration

    Config->>MySQL: Read Configuration Properties
    MySQL-->>Config: Configuration Data

    Gateway->>Eureka: Register Service
    UserService->>Eureka: Register Service
    BlogService->>Eureka: Register Service
    Config->>Eureka: Register Service

    Note over User,Gateway: User Login

    User->>Gateway: POST /user/api/users/login
    Gateway->>Eureka: Resolve USER-SERVICE
    Eureka-->>Gateway: USER-SERVICE Instance

    Gateway->>UserService: Forward Login Request

    UserService->>MySQL: Validate Credentials
    MySQL-->>UserService: User Found

    UserService->>UserService: Generate JWT Token

    UserService-->>Gateway: JWT Token
    Gateway-->>User: Login Success + JWT

    Note over User,Gateway: Create Blog

    User->>Gateway: POST /blog/api/blogs/createNewBlog (Bearer Token)

    Gateway->>Gateway: JwtHeaderFilter

    Gateway->>Eureka: Resolve BLOG-SERVICE
    Eureka-->>Gateway: BLOG-SERVICE Instance

    Gateway->>BlogService: Forward Request + JWT

    BlogService->>BlogService: Validate JWT

    BlogService->>Eureka: Resolve USER-SERVICE
    Eureka-->>BlogService: USER-SERVICE Instance

    BlogService->>UserService: GET /userDetails (JWT)

    UserService->>UserService: Validate JWT

    UserService->>MySQL: Fetch Logged-in User
    MySQL-->>UserService: User Details

    UserService-->>BlogService: UserInfo

    BlogService->>MongoDB: Save Blog
    MongoDB-->>BlogService: Blog Saved

    BlogService-->>Gateway: Blog Response
    Gateway-->>User: 201 Created

    Note over User,Gateway: Get All Blogs

    User->>Gateway: GET /blog/api/blogs/getAllBlogs (Bearer Token)

    Gateway->>Gateway: JwtHeaderFilter

    Gateway->>BlogService: Forward Request

    BlogService->>MongoDB: Fetch Blogs
    MongoDB-->>BlogService: Blog List

    loop For every Blog
        BlogService->>UserService: GET User Details by AuthorId
        UserService->>MySQL: Fetch Author
        MySQL-->>UserService: Author Details
        UserService-->>BlogService: UserInfo
    end

    BlogService-->>Gateway: Populated Blog Response
    Gateway-->>User: Blog List
```
## Database Schema (ER Diagram):

```mermaid
flowchart LR

subgraph UserService["USER-SERVICE (MySQL)"]

User["users
------------
id (PK)
username
email
password
role
verified"]

Otp["otps
------------
fpid (PK)
otpValue
expirationTime
user_id (FK)"]

User -->|1 : 1| Otp

end

subgraph ConfigService["CONFIG-SERVICE (MySQL)"]

Config["properties
------------
id (PK)
application
profile
label
value"]

end

subgraph BlogService["BLOG-SERVICE (MongoDB)"]

Blog["blogs
------------
_id
title
content
authorId"]

end

Blog -. Logical Reference .-> User
```

## Tech Stack Used:

#### Back-End:
<img alt="Spring-Boot" src="https://img.shields.io/badge/Spring-6DB33F?style=for-the-badge&logo=Spring-Boot&logoColor=white"/> <img alt="Hibernate" src="https://img.shields.io/badge/Hibernate-59666C?style=for-the-badge&logo=Hibernate&logoColor=white"/> <img alt="Swagger UI" src ="https://img.shields.io/badge/-Swagger-%23Clojure?style=for-the-badge&logo=swagger&logoColor=white"/> <img alt="JWT" src ="https://img.shields.io/badge/JWT-red?style=for-the-badge&logo=JSON+Web+Tokens&logoColor=white"/> 

#### Database:
<img alt="MySQL" src="https://img.shields.io/badge/mysql-%2300f.svg?style=for-the-badge&logo=mysql&logoColor=white"/><img alt="MongoDB" src ="https://img.shields.io/badge/MongoDB-4EA94B?style=for-the-badge&logo=mongodb&logoColor=white"/>

#### Deployed Version: 
https://springblogmicroservicediscovery-latest.onrender.com/

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












