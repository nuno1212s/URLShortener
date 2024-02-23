package me.nunogneto.models.shorteners;

public interface IShortURLGenerator {

    /**
     * Is this generator guaranteed to generate unique short URLs?
     *
     * @return true if the generator is guaranteed to generate unique short URLs, false otherwise
     */
    boolean isGuaranteedUnique();

    String generateShortURL(String originalURL);

    default String reGenShortURL(String originalURL, int attempts) {
        if (isGuaranteedUnique()) {
            throw new UnsupportedOperationException("This generator is guaranteed to generate unique short URLs");
        }

        throw new UnsupportedOperationException("This generator does not support re-generating short URLs");
    }

}
