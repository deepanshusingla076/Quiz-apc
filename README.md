# 🎯 QWIZZ – Interactive Quiz Platform

QWIZZ is a modern **Spring Boot + Thymeleaf** quiz application featuring **role-based dashboards (Student/Teacher)**, **JWT authentication**, and a bold **New Brutalist UI**.  
Built for **learning, authoring, and taking quizzes end-to-end**.  

---

## ✨ Features

### 🔥 Core Features
- 🔐 **User Authentication & Registration** – Secure login/register system  
- 🏠 **Interactive Landing Page** – Engaging homepage showcasing features  
- 📝 **Quiz Creation & Management** – Full CRUD operations for quizzes  
- 🧮 **Quiz Taking & Scoring** – Real-time scoring & feedback  
- 📊 **User Dashboard** – Track profile, progress, and quiz history  

### 🎨 Design Features
- 🖌️ **New Brutalist UI** – Bold, funky, cartoonish design  
- 📱 **Fully Responsive** – Works seamlessly across devices  
- 🎞️ **Interactive Animations** – Smooth transitions & hover effects  
- 🌈 **Vibrant Color Scheme** – Eye-catching palette  
- 🔡 **Modern Typography** – Bold fonts & creative layouts  

### 🛠 Technical Stack
- **Spring Boot 3.2 (Java 21)**  
- **Spring Security (JWT)** – Stateless authentication  
- **MySQL 8.0+** via Spring Data JPA  
- **Thymeleaf** + Layout & Security Dialects  
- **Custom CSS (Brutalist theme)**  

---

## 🚀 Getting Started

### ✅ Prerequisites
- Java 21  
- Maven 3.6+  
- MySQL 8.0+  
- Git  

### 📥 Installation
```bash
# Clone repository
git clone https://github.com/your-username/qwizz.git
cd qwizz
Setup Database (optional; auto-created if not present):

sql
Copy code
CREATE DATABASE qwizz_db;
CREATE USER 'qwizz_user'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON qwizz_db.* TO 'qwizz_user'@'localhost';
FLUSH PRIVILEGES;
Configure application.properties:

properties
Copy code
spring.datasource.url=jdbc:mysql://localhost:3306/qwizz_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=qwizz_user
spring.datasource.password=your_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# Server Port
server.port=9090
Build & Run:

bash
Copy code
mvn clean install
mvn spring-boot:run
Access the app 👉 http://localhost:9090

📁 Project Structure
bash
Copy code
qwizz/
├── src/
│   ├── main/
│   │   ├── java/com/qwizz/
│   │   │   ├── config/          # Security & configuration classes
│   │   │   ├── controller/      # Web controllers
│   │   │   ├── model/           # Entity models
│   │   │   ├── repository/      # Data access layer
│   │   │   ├── service/         # Business logic
│   │   │   └── QwizzApplication.java
│   │   └── resources/
│   │       ├── static/          # CSS, JS, images
│   │       │   ├── css/
│   │       │   └── js/
│   │       │       └── main.js
│   │       ├── templates/       # Thymeleaf templates
│   │       ├── application.properties
│   │       └── schema.sql       # Database schema
├── target/                      # Build output
├── pom.xml                      # Maven dependencies
├── .gitignore
└── README.md

🎮 Usage
🔑 Register/Login – Secure access with role-based dashboards

📝 Create Quizzes – Add questions, difficulty levels & time limits

🧮 Take Quizzes – Interactive UI with instant scoring

📊 Track Progress – Dashboard with history & analytics
