package me.nunogneto.infrastructure.mongo.dto;

import dev.morphia.annotations.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Entity
@Indexes(
        @Index(fields = @Field("originalURL"), options = @IndexOptions(unique = true))
)
@Getter
@NoArgsConstructor
public class MShortenedURLEntity {

    @Id
    private String shortenedURL;

    @Property("originalURL")
    private String originalURL;

    private Instant createdAt;

    public MShortenedURLEntity(String originalURL, String shortenedURL, ZonedDateTime createdAt) {
        this.originalURL = originalURL;
        this.shortenedURL = shortenedURL;
        this.createdAt = createdAt.withZoneSameInstant(ZoneId.of("UTC")).toInstant();
    }

}
