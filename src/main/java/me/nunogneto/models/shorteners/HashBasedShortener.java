package me.nunogneto.models.shorteners;

import lombok.AccessLevel;
import lombok.Getter;
import me.nunogneto.configuration.URLShortenerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class HashBasedShortener implements IShortURLGenerator {

    @Getter(value = AccessLevel.PROTECTED)
    private final Logger logger = LoggerFactory.getLogger(HashBasedShortener.class);

    private final URLShortenerConfiguration configuration;

    public HashBasedShortener(URLShortenerConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public boolean isGuaranteedUnique() {
        return false;
    }

    private String getHashFor(String originalURL) throws NoSuchAlgorithmException {
        MessageDigest instance = MessageDigest.getInstance("SHA-1");

        instance.update(originalURL.getBytes());

        byte[] digest = instance.digest();

        return Base64.getEncoder().encodeToString(digest);
    }

    @Override
    public String generateShortURL(String originalURL) {

        try {

            String shortURL = getHashFor(originalURL);

            return shortURL.substring(0, configuration.maxSize());

        } catch (NoSuchAlgorithmException e) {

            getLogger().error("Could not find Hashing algorithm", e);

            System.exit(0);

            return null;
        }
    }

    @Override
    public String reGenShortURL(String originalURL, int attempts) {
        try {

            String shortURL = getHashFor(originalURL);

            if (shortURL.length() < configuration.maxSize() + attempts) {
                throw new IllegalArgumentException("The original URL is too short to generate a short URL with the given configuration");
            }

            return shortURL.substring(attempts, configuration.maxSize() + attempts);
        } catch (NoSuchAlgorithmException e) {

            getLogger().error("Could not find Hashing algorithm", e);

            System.exit(0);

            return null;
        }
    }
}
