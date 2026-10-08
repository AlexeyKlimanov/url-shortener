package io.github.AlexeyKlimanov.url_shortener.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import io.github.AlexeyKlimanov.url_shortener.entity.User;
import io.github.AlexeyKlimanov.url_shortener.repository.UserRepository;
import io.jsonwebtoken.lang.Collections;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService{
    private final UserRepository userRepository;

    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException{
        User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new UsernameNotFoundException(
                                                    "User not found with email: " + email
                                            ));

        return org.springframework.security.core.userdetails.User
                                            .withUsername(user.getEmail())
                                            .password(user.getPasswordHash())
                                            .authorities(Collections.emptyList())
                                            .build();
    }
}