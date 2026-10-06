package io.github.AlexeyKlimanov.url_shortener.dto;

import jakarta.validation.constraints.*;

public record RegisterRequest(
    @NotBlank(message="Email обязателен!")
    @Email(message="Некоррктный email!")
    String email,
    @NotBlank(message="Пароль обязателен!")
    @Size(min=8, message="Пароль должен быть минимум 8 символов!")
    String password
) { }