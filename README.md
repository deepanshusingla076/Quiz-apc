🎯 QWIZZ – Interactive Quiz Platform

Modern Spring Boot + Thymeleaf quiz application with role-based dashboards (Student/Teacher), JWT authentication, and a bold New Brutalist UI.
Built for learning, authoring, and taking quizzes end-to-end.

✨ Features
🔥 Core Features

🔐 User Authentication & Registration – Secure login/register system

🏠 Interactive Landing Page – Engaging homepage showcasing features

📝 Quiz Creation & Management – Full CRUD operations for quizzes

🧮 Quiz Taking & Scoring – Real-time scoring & feedback

📊 User Dashboard – Track profile, progress, and quiz history

🌍 Browse Public Quizzes – Discover and attempt community-created quizzes

🎨 Design Features

🖌️ New Brutalist UI – Bold, funky, cartoonish design

📱 Fully Responsive – Works seamlessly across devices

🎞️ Interactive Animations – Smooth transitions & hover effects

🌈 Vibrant Color Scheme – Eye-catching palette

🔡 Modern Typography – Bold fonts and creative layouts

🛠 Technical Features

Spring Boot 3.2 (Java 21)

MySQL via Spring Data JPA

Thymeleaf + Layout & Security Dialects

Spring Security (JWT) with stateless sessions

Custom Responsive CSS (Brutalist theme)

🚀 Getting Started
Prerequisites

Java 21 installed

Maven 3.6+ installed

MySQL 8.0+ running locally

Git installed

Installation

Clone the Repository

git clone https://github.com/your-username/qwizz.git
cd qwizz


Setup MySQL Database (optional; auto-created if not present)

CREATE DATABASE qwizz_db;
CREATE USER 'qwizz_user'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON qwizz_db.* TO 'qwizz_user'@'localhost';
FLUSH PRIVILEGES;


Configure Database & Port in src/main/resources/application.properties:

spring.datasource.url=jdbc:mysql://localhost:3306/qwizz_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=your_mysql_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# Server Port
server.port=9090


Build & Run

mvn clean install
mvn spring-boot:run


Access the Application
👉 http://localhost:9090

📁 Project Structure
qwizz/
├── src/
│   ├── main/
│   │   ├── java/com/qwizz/
│   │   │   ├── config/          # Configuration classes
│   │   │   ├── controller/      # Web controllers
│   │   │   ├── model/           # Entity models
│   │   │   ├── repository/      # Data access layer
│   │   │   ├── service/         # Business logic
│   │   │   └── QwizzApplication.java
│   │   └── resources/
│   │       ├── static/          # CSS, JS, images
│   │       ├── templates/       # Thymeleaf templates
│   │       ├── application.properties
│   │       └── schema.sql       # Database schema
│   └── test/                    # Unit & integration tests
├── pom.xml                      # Maven dependencies
└── README.md

🎮 Usage

Register/Login – Secure access with role-based dashboards

Create Quizzes – Add questions, difficulty levels & time limits

Take Quizzes – Interactive UI with instant scoring

Track Progress – Dashboard with history & analytics

Browse Quizzes – Discover quizzes created by others

🤝 Contributing

We welcome contributions!

Fork the repository

Create a feature branch (git checkout -b feature/amazing-feature)

Commit your changes (git commit -m 'Add amazing feature')

Push to the branch (git push origin feature/amazing-feature)

Open a Pull Request

📄 License

This project is licensed under the MIT License – see the LICENSE
 file for details.

🙏 Acknowledgments

Spring Boot team

Thymeleaf

MySQL

Font Awesome

Open-source community
