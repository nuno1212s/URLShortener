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

    public ShortenURLService(URLShortenerConfiguration configuration, IShortURLGenerator generator,
                             IShortenedURLRepository repository, IEventPublisher publisher) {
        this.configuration = configuration;
        this.generator = generator;
        this.repository = repository;
        this.eventPublisher = publisher;
    }

    private Optional<ShortenedURLEntity> generateShortenedURL(String originalURL, int attempt) {

        ShortenedURLEntity entity;

        String shortenedURL = attempt == 0 ? generator.generateShortURL(originalURL) : generator.reGenShortURL(originalURL, attempt);

        entity = new ShortenedURLEntity(originalURL, shortenedURL, ZonedDateTime.now());

        if (repository.insertNew(entity)) {
            return Optional.of(entity);
        }

        return Optional.empty();
    }

    private void assertMatchesConfiguration(String shortURL) throws IllegalArgumentException {

        if (shortURL.length() != configuration.maxSize()) {
            throw new IllegalArgumentException("Shortened URL does not match the expected length");
        }

        if (!configuration.allowedCharacters().matcher(shortURL).matches()) {
            throw new IllegalArgumentException("Shortened URL does not match the expected pattern");
        }

    }

    private static final int MAX_ATTEMPTS = 10;

    @Override
    public ShortenedURLEntity shortenURL(String originalURL) throws IllegalArgumentException {

        int attempt = 0;

        ShortenedURLEntity shortenedURLEntity;

        while (true) {

            if (attempt > MAX_ATTEMPTS) {
                throw new IllegalArgumentException("Cannot shorten URL");
            }

            Optional<ShortenedURLEntity> byFullLengthURL = repository.findByOriginalURL(originalURL);

            if (byFullLengthURL.isPresent()) {
                shortenedURLEntity = byFullLengthURL.get();

                break;
            }

            Optional<ShortenedURLEntity> shortenedURL = generateShortenedURL(originalURL, attempt++);

            if (shortenedURL.isPresent()) {
                shortenedURLEntity = shortenedURL.get();
                break;
            }
        }

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
