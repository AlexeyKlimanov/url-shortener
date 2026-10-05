package io.github.AlexeyKlimanov.url_shortener.repository;

import io.github.AlexeyKlimanov.url_shortener.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>{
    Optional<User> findByEmail(String email);
    boolean existsEmail(String email);
}