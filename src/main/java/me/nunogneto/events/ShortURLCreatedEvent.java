package me.nunogneto.events;

import java.time.ZonedDateTime;

public record ShortURLCreatedEvent(String originalURL, String shortenedURL, ZonedDateTime createdAt) { }
