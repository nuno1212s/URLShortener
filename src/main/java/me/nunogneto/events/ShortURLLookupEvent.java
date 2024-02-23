package me.nunogneto.events;

import java.time.ZonedDateTime;

public record ShortURLLookupEvent(String shortURL, ZonedDateTime lookupTime) { }
