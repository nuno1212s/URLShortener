package me.nunogneto.repositorytests.cache;


import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import me.nunogneto.infrastructure.redis.RedisCacheRepository;
import me.nunogneto.models.ShortenedURLEntity;
import me.nunogneto.repositories.IShortenedURLRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

/**
 * Unit test for the Redis Caching layer which is implemented with a proxy pattern
 * to assure complete opacity to the service layer.
 */
@ExtendWith(MockitoExtension.class)
public class RedisCacheTest {

    private IShortenedURLRepository cacheRepository;

    @Mock
    private IShortenedURLRepository repository;

    private RedisClient client;

    private Logger logger = LoggerFactory.getLogger(RedisCacheTest.class);

    private static final String TEST_ID = "testID",
            TEST_ID_2 = "testID2",
            ORIGINAL_URL = "http://google.com";

    private static final ShortenedURLEntity TEST_ENTITY = new ShortenedURLEntity(ORIGINAL_URL, TEST_ID, ZonedDateTime.now().truncatedTo(ChronoUnit.MILLIS));


    public RedisCacheTest() {
        this.client = RedisClient.create("redis://172.20.0.4:6379");
    }

    @BeforeEach
    public void setUp() {

        this.cacheRepository = new RedisCacheRepository(repository, this.client);

        try (StatefulRedisConnection<String, String> connect = this.client.connect()) {

            connect.sync().del(TEST_ID);
            connect.sync().del(TEST_ID_2);
        } catch (Exception e) {
            logger.error("Failed to clean up redis test DB", e);
        }

        Mockito.reset(repository);
    }

    @Test
    public void testCacheMiss() {
        Mockito.when(this.repository.findById(TEST_ID)).thenReturn(Optional.of(TEST_ENTITY));

        Assertions.assertTrue(cacheRepository.findById(TEST_ID).isPresent());

        Mockito.verify(repository).findById(TEST_ID);

        Assertions.assertTrue(cacheRepository.findById(TEST_ID).isPresent());

        Mockito.verifyNoMoreInteractions(repository);
    }

    @Test
    public void testCacheHit() {
        // Repository should be called to insert, and should return true as we are not testing the repository
        Mockito.when(this.repository.insertNew(TEST_ENTITY)).thenReturn(true);

        cacheRepository.insertNew(TEST_ENTITY);

        Mockito.verify(repository).insertNew(TEST_ENTITY);

        Optional<ShortenedURLEntity> resultingURL = cacheRepository.findById(TEST_ID);

        // We should not have called the repository again, as it should have been cached
        Mockito.verifyNoMoreInteractions(repository);

        Assertions.assertTrue(resultingURL.isPresent());
        Assertions.assertEquals(TEST_ENTITY, resultingURL.get());
    }


}
