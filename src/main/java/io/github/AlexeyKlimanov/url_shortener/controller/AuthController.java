package io.github.AlexeyKlimanov.url_shortener.controller;

import io.github.AlexeyKlimanov.url_shortener.dto.RegisterRequest;
import io.github.AlexeyKlimanov.url_shortener.dto.UserResponse;
import io.github.AlexeyKlimanov.url_shortener.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userSirvice;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request){
        UserResponse response = userSirvice.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
