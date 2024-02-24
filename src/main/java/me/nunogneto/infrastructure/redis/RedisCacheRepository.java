package me.nunogneto.infrastructure.redis;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import lombok.AccessLevel;
import lombok.Getter;
import me.nunogneto.models.ShortenedURLEntity;
import me.nunogneto.repositories.IShortenedURLRepository;
import me.nunogneto.serialization.gsonadapters.ZonedDateTimeAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZonedDateTime;
import java.util.Optional;

/**
 * A Redis caching layer, implemented with
 * a Proxy pattern, to hide the implementation from the
 * services.
 * <p>
 * This will be a read-aside, write-aside caching layer in order
 * to promote availability and ease of use (and replace-ability)
 */
public class RedisCacheRepository implements IShortenedURLRepository {

    @Getter
    private final Gson gson;

    @Getter(value = AccessLevel.PROTECTED)
    private final StatefulRedisConnection<String, String> redisConn;

    private final IShortenedURLRepository repository;

    private final Logger logger = LoggerFactory.getLogger(RedisCacheRepository.class);

    public RedisCacheRepository(IShortenedURLRepository repository, RedisClient client) {
        this.repository = repository;

        this.redisConn = client.connect();

        this.gson = new GsonBuilder().registerTypeAdapter(ZonedDateTime.class, new ZonedDateTimeAdapter()).create();
    }

    @Override
    public Optional<ShortenedURLEntity> findById(String shortURL) {

        String jsonURLShort = redisConn.sync().get(shortURL);

        if (jsonURLShort != null) {
            ShortenedURLEntity entity = this.gson.fromJson(jsonURLShort, ShortenedURLEntity.class);

            return Optional.of(entity);
        }

        Optional<ShortenedURLEntity> shortenedURL = this.repository.findById(shortURL);

        shortenedURL.ifPresent(this::saveURLEntity);

        return shortenedURL;
    }

    private void saveURLEntity(ShortenedURLEntity entity) {
        String json = this.gson.toJson(entity);

        this.redisConn.sync().set(entity.shortenedURL(), json);
    }

    @Override
    public ShortenedURLEntity save(ShortenedURLEntity entity) {
        ShortenedURLEntity save = this.repository.save(entity);

        if (save != null) {
            saveURLEntity(save);
        }

        return save;
    }

    @Override
    public Optional<ShortenedURLEntity> findByOriginalURL(String originalURL) {
        return this.repository.findByOriginalURL(originalURL);
    }

    @Override
    public boolean insertNew(ShortenedURLEntity entity) {

        boolean result = this.repository.insertNew(entity);

        logger.warn("Inserting new URL into DB, returning: {}", result);

        if (result) {
            logger.info("Inserting new URL into cache");

            saveURLEntity(entity);
        }

        return result;
    }
}
