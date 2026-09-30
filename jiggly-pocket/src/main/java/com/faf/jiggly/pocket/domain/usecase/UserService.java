package com.faf.jiggly.pocket.domain.usecase;

import com.faf.jiggly.pocket.domain.model.DocumentDraft;
import com.faf.jiggly.pocket.domain.model.User;
import com.faf.jiggly.pocket.domain.model.UserId;
import com.faf.jiggly.pocket.domain.port.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.Optional;
@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User register(String email, String rawPassword) {
        var normalizedEmail = email.trim().toLowerCase();

        if (userRepository.findByEmail(normalizedEmail).isPresent()) {
            throw new IllegalStateException("Email already registered");
        }

           var hash = passwordEncoder.encode(rawPassword);   // BCrypt
        //    System.out.println("STORED HASH" + hash); //see the hash 


        return userRepository.save(new User(UserId.of(UUID.randomUUID()), normalizedEmail, hash));
    }


    public Optional<User> authenticate(String email, String rawPassword) {
    var normalizedEmail = email.trim().toLowerCase();
    return userRepository.findByEmail(normalizedEmail)
            .filter(user -> passwordEncoder.matches(rawPassword, user.passwordHash()));
    }

    



}