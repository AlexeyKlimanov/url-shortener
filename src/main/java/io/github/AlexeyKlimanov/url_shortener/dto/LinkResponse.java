package io.github.AlexeyKlimanov.url_shortener.dto;

import io.github.AlexeyKlimanov.url_shortener.entity.Link;

import java.time.LocalDateTime;

public record LinkResponse(
        Long id,
        String shortCode,
        String shortUrl,
        String originalUrl,
        Long clickCount,
        LocalDateTime createdAt
) {

    public static LinkResponse from(Link link, String baseUrl) {
        return new LinkResponse(
                link.getId(),
                link.getShortCode(),
                baseUrl + "/" + link.getShortCode(),
                link.getOriginalUrl(),
                link.getClickCount(),
                link.getCreatedAt()
        );
    }
}