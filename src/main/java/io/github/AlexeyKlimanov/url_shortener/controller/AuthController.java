package io.github.AlexeyKlimanov.url_shortener.controller;

import io.github.AlexeyKlimanov.url_shortener.dto.AuthResponse;
import io.github.AlexeyKlimanov.url_shortener.dto.LoginRequest;
import io.github.AlexeyKlimanov.url_shortener.dto.RegisterRequest;
import io.github.AlexeyKlimanov.url_shortener.dto.UserResponse;
import io.github.AlexeyKlimanov.url_shortener.service.JwtService;
import io.github.AlexeyKlimanov.url_shortener.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userSirvice;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request){
        UserResponse response = userSirvice.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request){
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.email(),
                request.password()
            )
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtService.generateToken(userDetails);
        AuthResponse response = new AuthResponse(
            token,
            "Bearer",
            jwtService.getExpirationSeconds()
        );
        return ResponseEntity.ok(response);
    }
}
