package io.github.AlexeyKlimanov.url_shortener.controller;

import io.github.AlexeyKlimanov.url_shortener.service.LinkService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequiredArgsConstructor
public class LinkRedirectController {

    private final LinkService linkService;

    @GetMapping("/{code:[a-zA-Z0-9]{4,10}}")
    public ResponseEntity<Void> redirect(@PathVariable String code) {
        String originalUrl = linkService.resolveCode(code);
        return ResponseEntity
                .status(HttpStatus.FOUND)
                .location(URI.create(originalUrl))
                .build();
    }
}