# QWIZZ – Interactive Quiz Platform

Modern Spring Boot + Thymeleaf quiz application with role-based dashboards (Student/Teacher), JWT authentication, and a bold “New Brutalist” UI. Built for learning, authoring, and taking quizzes end-to-end.

## ✨ Features

### 🔥 Core Features
- **User Authentication & Registration** - Secure login/register system
- **Interactive Landing Page** - Engaging homepage showcasing all features
- **Quiz Creation & Management** - Full CRUD operations for quizzes
- **AI-Powered Quiz Generation** - Generate quizzes based on topics and difficulty
- **Quiz Taking & Scoring** - Interactive quiz experience with real-time scoring
- **User Dashboard** - Comprehensive user profile and progress tracking
- **Browse Public Quizzes** - Discover and take community-created quizzes

### 🎨 Design Features
- **New Brutalist UI** - Bold, funky, and cartoonish design elements
- **Fully Responsive** - Works perfectly on all devices
- **Interactive Animations** - Smooth animations and hover effects
- **Vibrant Color Scheme** - Eye-catching color palette
- **Modern Typography** - Bold fonts and creative layouts

### 🛠 Technical Features
- **Spring Boot 3.2** (Java 17)
- **MySQL** via Spring Data JPA
- **Thymeleaf + Layout & Security Dialects**
- **Spring Security (JWT)** with stateless sessions
- **Responsive CSS** (custom brutalist theme)

## 🚀 Getting Started

### Prerequisites

Before running QWIZZ, make sure you have:

- **Java 17+** installed
- **Maven 3.6+** installed
- **MySQL 8.0+** running locally
- **Git** for cloning the repository

### Installation

1. **Clone the Repository**
   ```bash
   git clone https://github.com/your-username/qwizz.git
   cd qwizz
   ```

2. **Setup MySQL Database** (optional; DB auto-creates on first run)
   ```sql
   -- Create database (optional - app will create it automatically)
   CREATE DATABASE qwizz_db;
   
   -- Create a MySQL user (optional)
   CREATE USER 'qwizz_user'@'localhost' IDENTIFIED BY 'your_password';
   GRANT ALL PRIVILEGES ON qwizz_db.* TO 'qwizz_user'@'localhost';
   FLUSH PRIVILEGES;
   ```

3. **Configure Database Connection**
   
   Update `src/main/resources/application.properties`:
   ```properties
   # Database Configuration
   spring.datasource.url=jdbc:mysql://localhost:3306/qwizz_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
   spring.datasource.username=root
   spring.datasource.password=your_mysql_password
   spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
   ```

4. **Build and Run**
   ```bash
   # Build the project
   mvn clean compile
   
   # Run the application
   mvn spring-boot:run
   ```

5. **Access the Application**
   
   Open your browser and navigate to: `http://localhost:8080`

## 📁 Project Structure

```
qwizz/
├── src/
│   ├── main/
│   │   ├── java/com/qwizz/
│   │   │   ├── config/          # Configuration classes
│   │   │   ├── controller/      # Web controllers
│   │   │   ├── model/          # Entity models
│   │   │   ├── repository/     # Data access layer
│   │   │   ├── service/        # Business logic
│   │   │   └── QwizzApplication.java
│   │   └── resources/
│   │       ├── static/
│   │       │   ├── css/        # Stylesheets
│   │       │   └── js/         # JavaScript files
│   │       ├── templates/      # Thymeleaf templates
│   │       ├── application.properties
│   │       └── schema.sql      # Database schema
│   └── test/                   # Test files
├── pom.xml                     # Maven dependencies
└── README.md
```

## 🎮 Usage Guide

### For Users

1. **Register/Login**
   - Create an account or login with existing credentials
   - Example seeded users are available if you load `schema.sql` manually.

2. **Take Quizzes**
   - Browse public quizzes on the Browse page
   - Click "Start Quiz" to begin
   - Answer questions and get instant feedback

3. **Create Quizzes**
   - Use the "Create Quiz" feature
   - Add multiple question types (Multiple Choice, True/False, Short Answer)
   - Set difficulty levels and time limits

4. (Optional) AI Generation – UI hooks present; wire your provider to enable.

5. **Track Progress**
   - View your dashboard for statistics
   - Check quiz history and performance
   - Manage your created quizzes

### For Developers

#### Adding New Features

1. **New Model**: Add to `src/main/java/com/qwizz/model/`
2. **New Repository**: Add to `src/main/java/com/qwizz/repository/`
3. **New Service**: Add to `src/main/java/com/qwizz/service/`
4. **New Controller**: Add to `src/main/java/com/qwizz/controller/`
5. **New Template**: Add to `src/main/resources/templates/`

#### Database Schema

The application uses four main tables:
- `users` - User accounts and profiles
- `quizzes` - Quiz metadata and settings
- `questions` - Individual quiz questions
- `quiz_attempts` - User quiz attempt records

## 🎨 Design System (Brief)

### Color Palette
- **Primary**: `#FF6B35` (Orange)
- **Secondary**: `#00D2FF` (Cyan)
- **Accent**: `#FFE66D` (Yellow)
- **Success**: `#00F5A0` (Green)
- **Error**: `#FF5E5B` (Red)

### Typography
- **Primary Font**: Arial Black (Headers)
- **Secondary Font**: Arial (Body text)
- **Monospace**: Courier New (Code)

### Components
- Bold borders and shadows
- High contrast colors
- Playful animations
- Brutalist aesthetic elements

## 🤝 Contributing

We welcome contributions! Here's how to get started:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Development Guidelines

- Follow Java naming conventions
- Write meaningful commit messages
- Add comments for complex logic
- Test your changes thoroughly
- Update documentation as needed

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🧪 Build, Run, and Troubleshooting

### Common Issues

**Database Connection Error**
- Ensure MySQL is running on localhost:3306
- Check username/password in application.properties
- Verify database exists or enable auto-creation

**Port Already in Use**
- Change port in application.properties: `server.port=8081`
- Or stop the process using port 8080

**Build Failures**
- Ensure Java 17+ is installed
- Run `mvn clean install` to refresh dependencies

**JWT Secret (Production)**
- Set an environment variable and let Spring read it:
  - Windows PowerShell: `$env:JWT_SECRET = (openssl rand -base64 48)`
  - Or generate any strong base64/hex string and assign to `JWT_SECRET`.
- The app reads it via `jwt.secret=${JWT_SECRET:...}` in `application.properties`.

### Getting Help

- 📧 Email: support@qwizz.com
- 🐛 Issues: [GitHub Issues](https://github.com/your-username/qwizz/issues)
- 💬 Discussions: [GitHub Discussions](https://github.com/your-username/qwizz/discussions)

## 🙏 Acknowledgments

- Spring Boot team for the amazing framework
- Thymeleaf for excellent templating
- Font Awesome for beautiful icons
- MySQL for reliable database solution
- The open-source community for inspiration

---

**Made with ❤️ by the QWIZZ Team**

*Turn learning into an adventure with QWIZZ!* 🚀
