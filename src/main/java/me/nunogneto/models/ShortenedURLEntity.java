package me.nunogneto.models;

import java.time.ZonedDateTime;
import java.util.Objects;

public record ShortenedURLEntity(String originalURL, String shortenedURL, ZonedDateTime createdAt) {

    public ShortenedURLEntity {
        Objects.requireNonNull(originalURL);
        Objects.requireNonNull(shortenedURL);
        Objects.requireNonNull(createdAt);
    }

}
