package me.nunogneto.modeltests.shortenertests;

import me.nunogneto.configuration.URLShortenerConfiguration;
import me.nunogneto.models.shorteners.HashBasedShortener;
import me.nunogneto.models.shorteners.IShortURLGenerator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

public class HashBasedURLShortenerTest {

    private static final String BASE_URL = "http://localhost:8080/";

    private final URLShortenerConfiguration configuration;

    private IShortURLGenerator generator;

    public HashBasedURLShortenerTest() {
        configuration = new URLShortenerConfiguration(7, Pattern.compile("[a-zA-Z0-9+/]"));
        generator = new HashBasedShortener(configuration);
    }

    @Test
    public void testShortenURL() {

        String shortURL = generator.generateShortURL(BASE_URL);

        Assertions.assertNotNull(shortURL);
        Assertions.assertEquals(configuration.maxSize(), shortURL.length());
        Assertions.assertTrue(configuration.allowedCharacters().matcher(shortURL).matches());
    }

    /**
     * Two shortURLs generated should not collide even if they are closely related, and
     * generated at close times.
     */
    @Test
    public void assertNonCollidingShortenedURLs() {
        String shortURL1 = generator.generateShortURL(BASE_URL);
        String shortURL2 = generator.generateShortURL(BASE_URL + "/testing");

        Assertions.assertNotEquals(shortURL1, shortURL2);
    }

    @Test
    public void testReGenShortURL() {

        String shortURL = generator.generateShortURL(BASE_URL);

        Assertions.assertEquals(shortURL, generator.generateShortURL(BASE_URL));
        Assertions.assertNotEquals(shortURL, generator.reGenShortURL(BASE_URL, 1));
        Assertions.assertNotEquals(generator.reGenShortURL(BASE_URL, 1), generator.reGenShortURL(BASE_URL, 2));
        Assertions.assertNotEquals(shortURL, generator.reGenShortURL(BASE_URL, 2));
    }

}
