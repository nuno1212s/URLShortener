package me.nunogneto.services;

import me.nunogneto.models.ShortenedURLEntity;

import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Service to shorten URLs
 */
public interface IShortenURLService {

    ShortenedURLEntity shortenURL(String originalURL);

    Optional<ShortenedURLEntity> findByShortenedURL(String shortenedURL);

}
