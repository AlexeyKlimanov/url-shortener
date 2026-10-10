package io.github.AlexeyKlimanov.url_shortener.service;

import io.github.AlexeyKlimanov.url_shortener.repository.LinkRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShortCodeGeneratorTest {

    @Mock
    private LinkRepository linkRepository;

    @InjectMocks
    private ShortCodeGenerator generator;

    @Test
    void generateUniqueCode_shouldReturnCodeOfLength7() {
        when(linkRepository.existsByShortCode(anyString())).thenReturn(false);

        String code = generator.generateUniqueCode();

        assertThat(code).hasSize(7);
    }

    @Test
    void generateUniqueCode_shouldReturnAlphanumericCode() {
        when(linkRepository.existsByShortCode(anyString())).thenReturn(false);

        String code = generator.generateUniqueCode();

        assertThat(code).matches("[a-zA-Z0-9]{7}");
    }

    @Test
    void generateUniqueCode_shouldRetryWhenCodeExists() {
        when(linkRepository.existsByShortCode(anyString()))
                .thenReturn(true, true, true, false);

        String code = generator.generateUniqueCode();

        assertThat(code).isNotBlank();
        verify(linkRepository, times(4)).existsByShortCode(anyString());
    }

    @Test
    void generateUniqueCode_shouldThrowWhenAllAttemptsFail() {
        when(linkRepository.existsByShortCode(anyString())).thenReturn(true);

        assertThatThrownBy(() -> generator.generateUniqueCode())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Failed to generate unique short code");

        verify(linkRepository, times(10)).existsByShortCode(anyString());
    }

    @Test
    void generateUniqueCode_shouldReturnDifferentCodesAcrossCalls() {
        when(linkRepository.existsByShortCode(anyString())).thenReturn(false);

        String first = generator.generateUniqueCode();
        String second = generator.generateUniqueCode();

        assertThat(first).isNotEqualTo(second);
    }
}