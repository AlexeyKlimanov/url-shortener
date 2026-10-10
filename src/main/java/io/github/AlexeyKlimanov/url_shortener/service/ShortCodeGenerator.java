package io.github.AlexeyKlimanov.url_shortener.service;

import io.github.AlexeyKlimanov.url_shortener.repository.LinkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ShortCodeGenerator {

    private static final String ALPHABET =
            "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    private static final int CODE_LENGTH = 7;

    private static final Set<String> RESERVED = Set.of(
            "error", "login", "logout", "api", "auth",
            "admin", "links", "hello", "actuator",
            "static", "assets", "public"
    );

    private static final int MAX_ATTEMPTS = 10;

    private final SecureRandom random = new SecureRandom();
    private final LinkRepository linkRepository;

    public String generateUniqueCode() {
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            String code = generateRandomCode();

            if (RESERVED.contains(code)) {
                continue;
            }

            if (!linkRepository.existsByShortCode(code)) {
                return code;
            }
        }

        throw new IllegalStateException(
                "Failed to generate unique short code after " + MAX_ATTEMPTS + " attempts"
        );
    }

    private String generateRandomCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            int index = random.nextInt(ALPHABET.length());
            sb.append(ALPHABET.charAt(index));
        }
        return sb.toString();
    }
}