package me.nunogneto.services;

import me.nunogneto.configuration.URLShortenerConfiguration;
import me.nunogneto.events.IEventPublisher;
import me.nunogneto.events.ShortURLLookupEvent;
import me.nunogneto.models.ShortenedURLEntity;
import me.nunogneto.models.shorteners.IShortURLGenerator;
import me.nunogneto.repositories.IShortenedURLRepository;

import java.time.ZonedDateTime;
import java.util.Optional;

public class ShortenURLService implements IShortenURLService {

    private final URLShortenerConfiguration configuration;

    private final IShortURLGenerator generator;

    private final IShortenedURLRepository repository;

    private final IEventPublisher eventPublisher;

    public ShortenURLService(URLShortenerConfiguration configuration, IShortURLGenerator generator, IShortenedURLRepository repository, IEventPublisher publisher) {
        this.configuration = configuration;
        this.generator = generator;
        this.repository = repository;
        this.eventPublisher = publisher;
    }

    private ShortenedURLEntity generateShortenedURL(String originalURL) {

        ShortenedURLEntity entity;

        int attempts = 0;

        do {
            String shortenedURL = attempts == 0 ? generator.generateShortURL(originalURL) : generator.reGenShortURL(originalURL, attempts);

            attempts++;

            entity = new ShortenedURLEntity(originalURL, shortenedURL, ZonedDateTime.now());

        } while (repository.save(entity) == null);

        return entity;
    }

    private void assertMatchesConfiguration(String shortURL) throws IllegalArgumentException {

        if (shortURL.length() != configuration.maxSize()) {
            throw new IllegalArgumentException("Shortened URL does not match the expected length");
        }

        if (!configuration.allowedCharacters().matcher(shortURL).matches()) {
            throw new IllegalArgumentException("Shortened URL does not match the expected pattern");
        }

    }

    @Override
    public ShortenedURLEntity shortenURL(String originalURL) {
        // If we already have a shortened URL for the original URL, return it
        ShortenedURLEntity shortenedURLEntity = repository.findByOriginalURL(originalURL)
                .orElseGet(() -> generateShortenedURL(originalURL));

        eventPublisher.publish(new ShortURLLookupEvent(shortenedURLEntity.shortenedURL(), shortenedURLEntity.originalURL(), ZonedDateTime.now()));

        return shortenedURLEntity;
    }

    @Override
    public Optional<ShortenedURLEntity> findByShortenedURL(String shortenedURL) {
        // Assert we are being passed a valid shortened URL
        assertMatchesConfiguration(shortenedURL);

        Optional<ShortenedURLEntity> shortURL = repository.findById(shortenedURL);

        shortURL.ifPresent(shortenedURLEntity -> eventPublisher.publish(new ShortURLLookupEvent(shortenedURL, shortenedURLEntity.originalURL(), ZonedDateTime.now())));

        return shortURL;
    }

}
