package io.github.AlexeyKlimanov.url_shortener.controller;

import io.github.AlexeyKlimanov.url_shortener.dto.CreateLinkRequest;
import io.github.AlexeyKlimanov.url_shortener.dto.LinkResponse;
import io.github.AlexeyKlimanov.url_shortener.service.LinkService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class LinkController {

    private final LinkService linkService;

    @PostMapping("/api/links")
    public ResponseEntity<LinkResponse> createLink(
            @Valid @RequestBody CreateLinkRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        LinkResponse response = linkService.createLink(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/api/links")
    public List<LinkResponse> getMyLinks(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return linkService.getUserLinks(userDetails.getUsername());
    }
}