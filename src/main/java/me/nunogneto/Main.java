package me.nunogneto;

import me.nunogneto.configuration.URLShortenerConfiguration;
import me.nunogneto.events.IEventPublisher;
import me.nunogneto.infrastructure.mongo.MongoRepository;
import me.nunogneto.models.shorteners.HashBasedShortener;
import me.nunogneto.models.shorteners.IShortURLGenerator;
import me.nunogneto.repositories.IShortenedURLRepository;
import me.nunogneto.services.IShortenURLService;
import me.nunogneto.services.ShortenURLService;
import me.nunogneto.web.URLShortenerController;

import java.util.regex.Pattern;

public class Main {

    public static void main(String[] args) {

        URLShortenerConfiguration config = new URLShortenerConfiguration(7, Pattern.compile("[a-zA-Z0-9+/]"));

        IShortenedURLRepository repository = new MongoRepository();

        IShortURLGenerator generator = new HashBasedShortener(config);

        IShortenURLService service = new ShortenURLService(config, generator, repository, event -> {});

        new URLShortenerController(service);

    }
}