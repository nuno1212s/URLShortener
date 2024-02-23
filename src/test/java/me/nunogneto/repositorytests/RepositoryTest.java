package me.nunogneto.repositorytests;

import me.nunogneto.models.ShortenedURLEntity;
import me.nunogneto.models.shorteners.IShortURLGenerator;
import me.nunogneto.repositories.IShortenedURLRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class RepositoryTest {

    private IShortenedURLRepository repository;

    @Mock
    private IShortURLGenerator urlGenerator;

    public RepositoryTest() {
        this.repository = new me.nunogneto.infrastructure.mongo.MongoRepository();
    }

    @Test
    public void testRepository() {
        Mockito.when(urlGenerator.generateShortURL("http://www.google.com")).thenReturn("abc1234");

        String shortURL = this.urlGenerator.generateShortURL("http://www.google.com");

        ZonedDateTime currentTime = ZonedDateTime.now().truncatedTo(ChronoUnit.MILLIS);

        ShortenedURLEntity entity = new ShortenedURLEntity("http://www.google.com", shortURL, currentTime);

        this.repository.save(entity);

        Optional<ShortenedURLEntity> shortenedURL = this.repository.findById(shortURL);

        Assertions.assertTrue(shortenedURL.isPresent());
        Assertions.assertEquals(entity, shortenedURL.get());
    }

    @Test
    public void testRepositoryNotFound() {
        Optional<ShortenedURLEntity> shortenedURL = this.repository.findById("notfound");

        Assertions.assertTrue(shortenedURL.isEmpty());
    }

    @Test
    public void testRepeatShortURL() {

        Mockito.when(urlGenerator.generateShortURL("http://www.google.com")).thenReturn("abc1234");

        String shortURL = this.urlGenerator.generateShortURL("http://www.google.com");

        ShortenedURLEntity entity = new ShortenedURLEntity("http://www.google.com", shortURL, ZonedDateTime.now());

        this.repository.save(entity);

        Assertions.assertNull(this.repository.save(entity));
    }

    @Test
    public void testRepeatedOriginalURL() {



    }

}
