package io.github.AlexeyKlimanov.url_shortener.dto;

public record AuthResponse (
    String token,
    String tokenType,
    long expiresIn
) { }
