# URL Shortening service

A simple REST API to shorten URLs with a DDD design and TDD.

# REST Api made available

## GET /short/{url} -> resolve a shortened URL

Returns code 301 or 404 if it was not found
Malformed URLs will return 400

## POST /shorten -> shorten a URL

### Input is json representation of this form

```
public record URLShortenForm(String originalURL) { }
```

### Output will be a 200 with a response based on the response bellow. 
### Will return 400 for malformed inputs, 500 for internal server error while processing the shorten

```
public record URLShortenResponse(String originalURL, String shortenedURL) {}
```
