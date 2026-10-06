package io.github.AlexeyKlimanov.url_shortener.service;

import io.github.AlexeyKlimanov.url_shortener.dto.RegisterRequest;
import io.github.AlexeyKlimanov.url_shortener.dto.UserResponse;
import io.github.AlexeyKlimanov.url_shortener.entity.User;
import io.github.AlexeyKlimanov.url_shortener.repository.UserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService{
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserResponse register(RegisterRequest request){
        String email = request.email();

        if(userRepository.existsByEmail(email)){
            throw new IllegalArgumentException("Email уже занят!");
        }

        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));

        User saved = userRepository.save(user);

        return new UserResponse(saved.getId(), saved.getEmail(), saved.getCreatedAt());
    }
}