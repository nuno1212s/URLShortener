package me.nunogneto.servicetests;

import me.nunogneto.configuration.URLShortenerConfiguration;
import me.nunogneto.events.IEventPublisher;
import me.nunogneto.events.ShortURLCreatedEvent;
import me.nunogneto.events.ShortURLLookupEvent;
import me.nunogneto.models.ShortenedURLEntity;
import me.nunogneto.models.shorteners.IShortURLGenerator;
import me.nunogneto.repositories.IShortenedURLRepository;
import me.nunogneto.services.IShortenURLService;
import me.nunogneto.services.ShortenURLService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.regex.Pattern;

@ExtendWith(MockitoExtension.class)
public class URLServiceTest {

    private IShortenURLService service;

    @Mock
    private IShortURLGenerator generator;

    @Mock
    private IShortenedURLRepository repository;

    @Mock
    private IEventPublisher eventPublisher;

    public URLServiceTest() {
        URLShortenerConfiguration configuration = new URLShortenerConfiguration(7, Pattern.compile("^[a-zA-Z0-9]*$"));

        service = new ShortenURLService(configuration, generator, repository, eventPublisher);
    }

    private void testWrongURL(String url) {
        Assertions.assertThrows(IllegalArgumentException.class, () -> service.findByShortenedURL(url));

        // The service should not interact with the repository or the generator
        // If we get an invalid URL
        Mockito.verifyNoInteractions(repository, generator);
    }

    @Test
    public void testShortenURLWrongSize() {
        testWrongURL("1234578910");
    }

    @Test
    public void testShortenURLWrongPattern() {
        testWrongURL("12345@!");
    }

    @Test
    public void testValidURL() {
        Optional<ShortenedURLEntity> shortenedURL = service.findByShortenedURL("1234567");

        Assertions.assertTrue(shortenedURL.isEmpty());

        Mockito.verify(repository).findById("1234567");
        // Verify the correct event was published
        Mockito.verify(eventPublisher).publish(Mockito.any(ShortURLLookupEvent.class));
    }

    @Test
    public void testValidPresentURL() {
        ShortenedURLEntity shortenedURL = new ShortenedURLEntity("http://www.google.com", "1234567", ZonedDateTime.now());

        Mockito.when(repository.findById("1234567")).thenReturn(Optional.of(shortenedURL));

        Optional<ShortenedURLEntity> result = service.findByShortenedURL("1234567");

        Assertions.assertTrue(result.isPresent());

        Assertions.assertEquals(shortenedURL, result.get());

        Mockito.verify(repository).findById("1234567");
        Mockito.verify(eventPublisher).publish(Mockito.any(ShortURLLookupEvent.class));
    }

    @Test
    public void testURLShorten() {
        Mockito.when(generator.generateShortURL("http://www.google.com")).thenReturn("1234567");

        ShortenedURLEntity shortenedURL = service.shortenURL("http://www.google.com");

        Assertions.assertEquals("1234567", shortenedURL.shortenedURL());

        Mockito.verify(repository).save(shortenedURL);
        Mockito.verify(eventPublisher).publish(Mockito.any(ShortURLCreatedEvent.class));
    }

    @Test
    public void testRepeatedURL() {

    }

}
