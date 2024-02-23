package me.nunogneto.infrastructure.mongo;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import dev.morphia.Datastore;
import dev.morphia.InsertOneOptions;
import dev.morphia.Morphia;
import dev.morphia.query.filters.Filters;
import me.nunogneto.infrastructure.mongo.dto.MShortenedURLEntity;
import me.nunogneto.models.ShortenedURLEntity;
import me.nunogneto.repositories.IShortenedURLRepository;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

public class MongoRepository implements IShortenedURLRepository {

    private final Datastore datastore;

    public MongoRepository() {

        String user = System.getenv("MONGO_DB_USERNAME");
        String password = System.getenv("MONGO_DB_PASSWORD");
        String host = System.getenv("MONGO_DB_HOST");

        ConnectionString connectionString = new ConnectionString(String.format("mongodb://%s:%s@%s/?maxPoolSize=20", user, password, host));

        MongoClient mongoClient = MongoClients.create(connectionString);

        this.datastore = Morphia.createDatastore(mongoClient, "urls");
    }

    private static ZonedDateTime moveZonedData(Instant instant) {
        return instant.atZone(ZoneId.systemDefault());
    }

    @Override
    public Optional<ShortenedURLEntity> findById(String shortURL) {

        MShortenedURLEntity shortenedURL = this.datastore.find(MShortenedURLEntity.class)
                .filter(Filters.eq("shortenedURL", shortURL)).first();

        if (shortenedURL != null) {
            return Optional.of(new ShortenedURLEntity(shortenedURL.getOriginalURL(),
                    shortenedURL.getShortenedURL(),
                    moveZonedData(shortenedURL.getCreatedAt())));
        }

        return Optional.empty();
    }

    @Override
    public ShortenedURLEntity save(ShortenedURLEntity entity) {

        MShortenedURLEntity mShortenedURLEntity = new MShortenedURLEntity(entity.originalURL(), entity.shortenedURL(), entity.createdAt());

        try {
            this.datastore.insert(mShortenedURLEntity);
        } catch (Exception e) {
            return null;
        }

        return entity;
    }

    @Override
    public Optional<ShortenedURLEntity> findByOriginalURL(String originalURL) {

        MShortenedURLEntity shortenedURL = this.datastore.find(MShortenedURLEntity.class)
                .filter(Filters.eq("originalURL", originalURL)).first();

        if (shortenedURL != null) {
            return Optional.of(new ShortenedURLEntity(shortenedURL.getOriginalURL(),
                    shortenedURL.getShortenedURL(),
                    moveZonedData(shortenedURL.getCreatedAt())));
        }

        return Optional.empty();
    }

    @Override
    public boolean insertNew(ShortenedURLEntity entity) {
        return false;
    }
}
