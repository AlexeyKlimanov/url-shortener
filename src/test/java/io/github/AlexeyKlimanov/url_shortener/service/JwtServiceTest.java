package io.github.AlexeyKlimanov.url_shortener.service;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import io.jsonwebtoken.ExpiredJwtException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {
    private static final String TEST_SECRET =
                    "dGVzdC1zZWNyZXQta2V5LWZvci1qd3QtdGVzdHMtMzJieXRlcw==";
    private static final long EXPIRATION_MS = 86_400_000L;
    private JwtService jwtService;
    private UserDetails userDetails;

    @BeforeEach
    void setUp(){
        jwtService = new JwtService(TEST_SECRET, EXPIRATION_MS);
        userDetails = User.withUsername("alex@mail.ru")
                            .password("irrelevant-hash")
                            .authorities(Collections.emptyList())
                            .build();
    }

    @Test
    void generateToken_shouldProduceNonEmptyToken(){
        String token = jwtService.generateToken(userDetails);

        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    void extractUsername_shouldReturnEmailFromToken() {
        String token = jwtService.generateToken(userDetails);

        String username = jwtService.extractUsername(token);

        assertThat(username).isEqualTo("alex@mail.ru");
    }

    @Test
    void isTokenValid_shouldReturnTrueForFreshToken() {
        String token = jwtService.generateToken(userDetails);

        assertThat(jwtService.isTokenValid(token, userDetails)).isTrue();
    }

    @Test
    void isTokenValid_shouldReturnFalseForDifferentUser() {
        String token = jwtService.generateToken(userDetails);

        UserDetails otherUser = User.withUsername("other@mail.ru")
                .password("irrelevant")
                .authorities(Collections.emptyList())
                .build();

        assertThat(jwtService.isTokenValid(token, otherUser)).isFalse();
    }

    @Test
    void isTokenValid_shouldReturnFalseForExpiredToken() {
        JwtService shortLivedJwtService = new JwtService(TEST_SECRET, -1000L);
        String expiredToken = shortLivedJwtService.generateToken(userDetails);

        assertThatThrownBy(() -> shortLivedJwtService.isTokenValid(expiredToken, userDetails))
            .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void getExpirationSeconds_shouldReturnCorrectValue() {
        assertThat(jwtService.getExpirationSeconds()).isEqualTo(86_400L);
    }
}
