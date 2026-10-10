package io.github.AlexeyKlimanov.url_shortener.service;

import io.github.AlexeyKlimanov.url_shortener.dto.CreateLinkRequest;
import io.github.AlexeyKlimanov.url_shortener.dto.LinkResponse;
import io.github.AlexeyKlimanov.url_shortener.entity.Link;
import io.github.AlexeyKlimanov.url_shortener.entity.User;
import io.github.AlexeyKlimanov.url_shortener.repository.LinkRepository;
import io.github.AlexeyKlimanov.url_shortener.repository.UserRepository;
import io.github.AlexeyKlimanov.url_shortener.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LinkService {

    private static final int MAX_CREATE_ATTEMPTS = 5;

    private final LinkRepository linkRepository;
    private final UserRepository userRepository;
    private final ShortCodeGenerator codeGenerator;

    @Value("${app.base-url}")
    private String baseUrl;

    @Transactional
    public LinkResponse createLink(CreateLinkRequest request, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalStateException(
                        "Authenticated user not found: " + userEmail));

        return linkRepository
                .findByOriginalUrlAndUserId(request.originalUrl(), user.getId())
                .map(existing -> LinkResponse.from(existing, baseUrl))
                .orElseGet(() -> createNewLink(request.originalUrl(), user));
    }

    @Transactional(readOnly = true)
    public List<LinkResponse> getUserLinks(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalStateException(
                        "Authenticated user not found: " + userEmail));

        return linkRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(link -> LinkResponse.from(link, baseUrl))
                .toList();
    }

    @Transactional
    public String resolveCode(String shortCode) {
        Link link = linkRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Short link not found: " + shortCode));

        linkRepository.incrementClickCount(link.getId());
        return link.getOriginalUrl();
    }

    private LinkResponse createNewLink(String originalUrl, User user) {
        for (int attempt = 0; attempt < MAX_CREATE_ATTEMPTS; attempt++) {
            String code = codeGenerator.generateUniqueCode();
            try {
                Link link = Link.builder()
                        .shortCode(code)
                        .originalUrl(originalUrl)
                        .user(user)
                        .build();

                Link saved = linkRepository.saveAndFlush(link);
                log.info("Created short link {} for user {}", code, user.getEmail());
                return LinkResponse.from(saved, baseUrl);

            } catch (DataIntegrityViolationException ex) {
                log.warn("Collision on code {} (attempt {}), retrying", code, attempt + 1);
            }
        }
        throw new IllegalStateException(
                "Failed to create link after " + MAX_CREATE_ATTEMPTS + " attempts");
    }
}