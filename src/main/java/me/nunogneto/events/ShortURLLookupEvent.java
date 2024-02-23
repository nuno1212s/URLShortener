package me.nunogneto.events;

import java.time.ZonedDateTime;

public record ShortURLLookupEvent(String shortURL, String originalURL, ZonedDateTime lookupTime) { }
