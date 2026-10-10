package io.github.AlexeyKlimanov.url_shortener.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateLinkRequest(

        @NotBlank(message = "URL is required")
        @Size(max = 2048, message = "URL must not exceed 2048 characters")
        @Pattern(
                regexp = "^https?://.+",
                message = "URL must start with http:// or https://"
        )
        String originalUrl
) {
}