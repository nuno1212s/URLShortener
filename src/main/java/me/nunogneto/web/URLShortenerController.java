package me.nunogneto.web;

import com.google.gson.Gson;
import lombok.AccessLevel;
import lombok.Getter;
import me.nunogneto.models.ShortenedURLEntity;
import me.nunogneto.services.IShortenURLService;
import me.nunogneto.web.forms.URLShortenForm;
import me.nunogneto.web.responses.URLShortenResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;

import static spark.Spark.get;
import static spark.Spark.post;

public class URLShortenerController {

    @Getter(value = AccessLevel.PROTECTED)
    private IShortenURLService service;

    @Getter(value = AccessLevel.PROTECTED)
    private final Gson gson = new Gson();

    private final Logger logger = LoggerFactory.getLogger(URLShortenerController.class);

    public URLShortenerController(IShortenURLService service) {
        this.service = service;

        logger.info("Setting up routes");

        setupRoutes();
    }

    private void setupRoutes() {

        get("/short/:url", this::handleShortURLDiscovery);

        post("/shorten", this::handleShortURLCreation, gson::toJson);

    }

    private Object handleShortURLDiscovery(Request request, Response response) {

        String shortenedURL = request.params("url");

        getService().findByShortenedURL(shortenedURL)
                .ifPresentOrElse(
                        shortenedURLEntity -> response.redirect(shortenedURLEntity.originalURL(), 301),
                        () -> response.status(404));

        return null;
    }

    private Object handleShortURLCreation(Request request, Response response) {

        URLShortenForm shortenForm = gson.fromJson(request.body(), URLShortenForm.class);

        try {
            ShortenedURLEntity shortenedURLEntity = service.shortenURL(shortenForm.originalURL());

            URLShortenResponse rqResponse = new URLShortenResponse(shortenedURLEntity.originalURL(), shortenedURLEntity.shortenedURL());

            response.status(200);
            response.type("application/json");

            return rqResponse;

        } catch (IllegalArgumentException e) {
            response.status(400);

            return e.getMessage();
        } catch (Exception e) {
            response.status(500);

            return e.getMessage();
        }
    }

}
