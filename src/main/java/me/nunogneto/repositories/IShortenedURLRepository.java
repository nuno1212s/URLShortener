package me.nunogneto.repositories;

import me.nunogneto.models.ShortenedURLEntity;

import java.util.Optional;

public interface IShortenedURLRepository extends IRepository<ShortenedURLEntity, String> {

    Optional<ShortenedURLEntity> findByOriginalURL(String originalURL);


    boolean insertNew(ShortenedURLEntity entity);

}
