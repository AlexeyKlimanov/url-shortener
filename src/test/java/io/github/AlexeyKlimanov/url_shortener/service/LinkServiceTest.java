package io.github.AlexeyKlimanov.url_shortener.service;

import io.github.AlexeyKlimanov.url_shortener.dto.CreateLinkRequest;
import io.github.AlexeyKlimanov.url_shortener.dto.LinkResponse;
import io.github.AlexeyKlimanov.url_shortener.entity.Link;
import io.github.AlexeyKlimanov.url_shortener.entity.User;
import io.github.AlexeyKlimanov.url_shortener.exception.ResourceNotFoundException;
import io.github.AlexeyKlimanov.url_shortener.repository.LinkRepository;
import io.github.AlexeyKlimanov.url_shortener.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LinkServiceTest {

    private static final String BASE_URL = "http://localhost:8080";
    private static final String USER_EMAIL = "alex@mail.ru";
    private static final String ORIGINAL_URL = "https://www.google.com";
    private static final String SHORT_CODE = "aB3xY9q";

    @Mock
    private LinkRepository linkRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ShortCodeGenerator codeGenerator;

    @InjectMocks
    private LinkService linkService;

    private User user;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(linkService, "baseUrl", BASE_URL);

        user = new User();
        user.setId(1L);
        user.setEmail(USER_EMAIL);
    }

    @Test
    void createLink_shouldCreateNewLinkWhenUrlNotExists() {
        CreateLinkRequest request = new CreateLinkRequest(ORIGINAL_URL);

        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
        when(linkRepository.findByOriginalUrlAndUserId(ORIGINAL_URL, 1L))
                .thenReturn(Optional.empty());
        when(codeGenerator.generateUniqueCode()).thenReturn(SHORT_CODE);
        when(linkRepository.saveAndFlush(any(Link.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        LinkResponse response = linkService.createLink(request, USER_EMAIL);

        assertThat(response.shortCode()).isEqualTo(SHORT_CODE);
        assertThat(response.originalUrl()).isEqualTo(ORIGINAL_URL);
        assertThat(response.shortUrl()).isEqualTo(BASE_URL + "/" + SHORT_CODE);

        verify(linkRepository).saveAndFlush(any(Link.class));
    }

    @Test
    void createLink_shouldReturnExistingLinkWhenUrlAlreadyExists() {
        CreateLinkRequest request = new CreateLinkRequest(ORIGINAL_URL);
        Link existingLink = buildLink(42L, SHORT_CODE, ORIGINAL_URL, 5L);

        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
        when(linkRepository.findByOriginalUrlAndUserId(ORIGINAL_URL, 1L))
                .thenReturn(Optional.of(existingLink));

        LinkResponse response = linkService.createLink(request, USER_EMAIL);

        assertThat(response.id()).isEqualTo(42L);
        assertThat(response.shortCode()).isEqualTo(SHORT_CODE);
        assertThat(response.clickCount()).isEqualTo(5L);

        verify(linkRepository, never()).saveAndFlush(any());
        verify(codeGenerator, never()).generateUniqueCode();
    }

    @Test
    void createLink_shouldThrowWhenUserNotFound() {
        CreateLinkRequest request = new CreateLinkRequest(ORIGINAL_URL);
        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> linkService.createLink(request, USER_EMAIL))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Authenticated user not found");

        verify(linkRepository, never()).saveAndFlush(any());
    }

    @Test
    void getUserLinks_shouldReturnMappedList() {
        Link link1 = buildLink(1L, "code111", "https://a.com", 0L);
        Link link2 = buildLink(2L, "code222", "https://b.com", 3L);

        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
        when(linkRepository.findByUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(link1, link2));

        List<LinkResponse> result = linkService.getUserLinks(USER_EMAIL);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).shortCode()).isEqualTo("code111");
        assertThat(result.get(1).shortCode()).isEqualTo("code222");
        assertThat(result.get(0).shortUrl()).isEqualTo(BASE_URL + "/code111");
    }

    @Test
    void getUserLinks_shouldReturnEmptyListWhenNoLinks() {
        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
        when(linkRepository.findByUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of());

        List<LinkResponse> result = linkService.getUserLinks(USER_EMAIL);

        assertThat(result).isEmpty();
    }

    @Test
    void getUserLinks_shouldThrowWhenUserNotFound() {
        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> linkService.getUserLinks(USER_EMAIL))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void resolveCode_shouldReturnOriginalUrlAndIncrementClickCount() {
        Link link = buildLink(42L, SHORT_CODE, ORIGINAL_URL, 10L);

        when(linkRepository.findByShortCode(SHORT_CODE)).thenReturn(Optional.of(link));

        String result = linkService.resolveCode(SHORT_CODE);

        assertThat(result).isEqualTo(ORIGINAL_URL);
        verify(linkRepository).incrementClickCount(42L);
    }

    @Test
    void resolveCode_shouldThrowWhenCodeNotFound() {
        when(linkRepository.findByShortCode(SHORT_CODE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> linkService.resolveCode(SHORT_CODE))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(SHORT_CODE);

        verify(linkRepository, never()).incrementClickCount(any());
    }

    private Link buildLink(Long id, String code, String url, Long clickCount) {
        return Link.builder()
                .id(id)
                .shortCode(code)
                .originalUrl(url)
                .user(user)
                .clickCount(clickCount)
                .createdAt(LocalDateTime.now())
                .build();
    }
}