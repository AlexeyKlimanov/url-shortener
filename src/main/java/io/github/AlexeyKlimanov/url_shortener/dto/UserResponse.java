package io.github.AlexeyKlimanov.url_shortener.dto;

import java.time.LocalDateTime;

public record UserResponse (
    Long id,
    String email,
    LocalDateTime createdAt
) { }
