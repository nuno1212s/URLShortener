package me.nunogneto.modeltests;


import me.nunogneto.models.ShortenedURLEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;

public class ShortenURLTest {


    private static final String originalURL = "https://www.google.com";
    private static final String shortenedURL = "testshort";

    @Test
    public void testShortenURL() {

        /// Assert none of the fields are switched
        ShortenedURLEntity shortenedURLEntity = new ShortenedURLEntity(originalURL, shortenedURL, ZonedDateTime.now());

        Assertions.assertEquals(shortenedURLEntity.shortenedURL(), shortenedURL);
        Assertions.assertEquals(shortenedURLEntity.originalURL(), originalURL);
    }

    @Test
    public void testNullURL() {
        Assertions.assertThrows(NullPointerException.class, () -> {
            new ShortenedURLEntity(null, shortenedURL, ZonedDateTime.now());
        });
    }

    @Test
    public void testNullShortenedURL() {
        Assertions.assertThrows(NullPointerException.class, () -> {
            new ShortenedURLEntity(originalURL, null, ZonedDateTime.now());
        });
    }

    @Test
    public void testNullCreatedAt() {
        Assertions.assertThrows(NullPointerException.class, () -> {
            new ShortenedURLEntity(originalURL, shortenedURL, null);
        });
    }

}
