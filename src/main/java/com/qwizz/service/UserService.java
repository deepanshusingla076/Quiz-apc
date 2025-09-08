package com.qwizz.service;

import com.qwizz.model.User;
import com.qwizz.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    public User registerUser(User user) {
        // Check if username or email already exists
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // Encrypt password
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setActive(true);

        return userRepository.save(user);
    }

    public boolean authenticateUser(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            return passwordEncoder.matches(password, user.getPassword());
        }
        return false;
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User updateUser(User user) {
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    public User updateUserProfile(Long userId, String firstName, String lastName, String email) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();

            // Check if new email is already taken by another user
            if (!user.getEmail().equals(email) && userRepository.existsByEmail(email)) {
                throw new RuntimeException("Email already exists");
            }

            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setEmail(email);
            user.setUpdatedAt(LocalDateTime.now());

            return userRepository.save(user);
        }
        throw new RuntimeException("User not found");
    }

    public void changePassword(Long userId, String oldPassword, String newPassword) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();

            if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
                throw new RuntimeException("Current password is incorrect");
            }

            user.setPassword(passwordEncoder.encode(newPassword));
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
        } else {
            throw new RuntimeException("User not found");
        }
    }

    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public boolean isUsernameAvailable(String username) {
        if (username == null || username.trim().isEmpty() || username.trim().length() < 3) {
            return false;
        }
        return !userRepository.existsByUsername(username.trim());
    }

    public boolean isEmailAvailable(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return !userRepository.existsByEmail(email.trim());
    }

    public List<User> getUsersByRole(com.qwizz.model.Role role) {
        return userRepository.findByRoleAndActiveTrue(role);
    }
    
    // Enhanced methods for dashboard and statistics
    
    public List<User> getTopUsersByPoints() {
        return userRepository.findTop10ByOrderByTotalPointsDesc();
    }
    
    public List<User> getUsersByStreak(Integer minStreak) {
        return userRepository.findByQuizStreakGreaterThanOrderByQuizStreakDesc(minStreak);
    }
    
    public List<User> getUsersByAverageScore(Double minScore) {
        return userRepository.findByAverageScoreGreaterThanEqual(minScore);
    }
    
    public List<User> getMostActiveUsers() {
        return userRepository.findMostActiveUsers();
    }
    
    public List<User> getTopCreators() {
        return userRepository.findTopCreators();
    }
    
    public List<User> searchUsers(String search) {
        return userRepository.searchUsers(search);
    }
    
    public List<User> getUsersByLocation(String location) {
        return userRepository.findByLocationContainingIgnoreCaseAndActiveTrue(location);
    }
    
    public int getUserCountByRole(com.qwizz.model.Role role) {
        return userRepository.countByRole(role);
    }
    
    public List<User> getRecentUsers(LocalDateTime since) {
        return userRepository.findRecentUsers(since);
    }
    
    public void updateUserLastLogin(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);
        }
    }
    
    public void updateUserStatistics(User user, int pointsEarned, double quizScore, boolean maintainedStreak) {
        user.addPoints(pointsEarned);
        user.incrementQuizzesTaken();
        user.updateAverageScore(quizScore);
        
        if (maintainedStreak) {
            user.setQuizStreak(user.getQuizStreak() + 1);
        } else {
            user.setQuizStreak(0);
        }
        
        userRepository.save(user);
    }
    
    public void incrementUserQuizzesCreated(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.incrementQuizzesCreated();
            userRepository.save(user);
        }
    }
    
    public User updateUserProfile(User user, String bio, String phone, String location, java.sql.Date dateOfBirth) {
        user.setBio(bio);
        user.setPhone(phone);
        user.setLocation(location);
        user.setDateOfBirth(dateOfBirth);
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }
    
    public User updateProfilePicture(Long userId, String profilePictureUrl) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setProfilePicture(profilePictureUrl);
            user.setUpdatedAt(LocalDateTime.now());
            return userRepository.save(user);
        }
        throw new RuntimeException("User not found");
    }
    
    public void verifyUserEmail(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setEmailVerified(true);
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
        }
    }
}
